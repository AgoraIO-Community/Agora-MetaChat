package io.agora.metachat.example;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import io.agora.metachat.IMetachatEventHandler;
import io.agora.metachat.IMetachatScene;
import io.agora.metachat.IMetachatSceneEventHandler;
import io.agora.metachat.IMetachatService;
import io.agora.metachat.MetachatConfig;
import io.agora.metachat.MetachatSceneInfo;
import io.agora.metachat.MetachatUserAvatarConfig;
import io.agora.metachat.MetachatUserInfo;
import io.agora.metachat.MetachatUserPositionInfo;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.spatialaudio.ILocalSpatialAudioEngine;
import io.agora.spatialaudio.LocalSpatialAudioConfig;
import io.agora.spatialaudio.RemoteVoicePositionInfo;

public class MetaChatContext implements IMetachatEventHandler, IMetachatSceneEventHandler {

    private final static String TAG = MetaChatContext.class.getName();
    private volatile static MetaChatContext instance = null;

    private RtcEngine rtcEngine;
    private ILocalSpatialAudioEngine spatialAudioEngine;
    private IMetachatService metaChatService;
    private IMetachatScene metaChatScene;
    private MetachatSceneInfo sceneInfo;
    private MetachatUserAvatarConfig avatarConfig;
    private String roomName;
    private final ConcurrentHashMap<IMetachatEventHandler, Integer> metaChatEventHandlerMap;
    private final ConcurrentHashMap<IMetachatSceneEventHandler, Integer> metaChatSceneEventHandlerMap;

    private MetaChatContext() {
        metaChatEventHandlerMap = new ConcurrentHashMap<>();
        metaChatSceneEventHandlerMap = new ConcurrentHashMap<>();
    }

    public static MetaChatContext getInstance() {
        if (instance == null) {
            synchronized (MetaChatContext.class) {
                if (instance == null) {
                    instance = new MetaChatContext();
                }
            }
        }
        return instance;
    }

    public boolean initialize(Context context, @Nullable String nickname, @Nullable String avatar) {
        int ret = Constants.ERR_OK;
        if (rtcEngine == null) {
            try {
                rtcEngine = RtcEngine.create(context, KeyCenter.RTC_APP_ID, new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        Log.d(TAG, String.format("onJoinChannelSuccess %s %d", channel, uid));
                    }

                    @Override
                    public void onAudioRouteChanged(int routing) {
                        Log.d(TAG, String.format("onAudioRouteChanged %d", routing));
                    }
                });
                rtcEngine.enableAudio();
                rtcEngine.disableVideo();
                rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                rtcEngine.setAudioProfile(
                        Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING
                );

                {
                    spatialAudioEngine = ILocalSpatialAudioEngine.create();
                    LocalSpatialAudioConfig config = new LocalSpatialAudioConfig() {{
                        mRtcEngine = rtcEngine;
                    }};
                    ret += spatialAudioEngine.initialize(config);
                    // ILocalSpatialAudioEngine统一管理audio的mute状态
                    spatialAudioEngine.muteLocalAudioStream(false);
                    spatialAudioEngine.muteAllRemoteAudioStreams(false);
                }

                {
                    metaChatService = IMetachatService.create();
                    MetachatConfig config = new MetachatConfig() {{
                        mRtcEngine = rtcEngine;
                        mAppId = KeyCenter.RTM_APP_ID;
                        mToken = KeyCenter.RTM_TOKEN;
                        mLocalDownloadPath = context.getExternalCacheDir().getPath();
                        mUserInfo = new MetachatUserInfo() {{
                            mUserId = KeyCenter.RTM_UID;
                            mUserName = nickname == null ? mUserId : nickname;
                            mUserIconUrl = avatar == null ? "https://accpic.sd-rtn.com/pic/test/png/2.png" : avatar;
                        }};
                        mEventHandler = MetaChatContext.this;
                    }};
                    ret += metaChatService.initialize(config);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return ret == Constants.ERR_OK;
    }

    public void destroy() {
        IMetachatService.destroy();
        metaChatService = null;
        ILocalSpatialAudioEngine.destroy();
        spatialAudioEngine = null;
        RtcEngine.destroy();
        rtcEngine = null;
    }

    public void registerMetaChatEventHandler(IMetachatEventHandler eventHandler) {
        metaChatEventHandlerMap.put(eventHandler, 0);
    }

    public void unregisterMetaChatEventHandler(IMetachatEventHandler eventHandler) {
        metaChatEventHandlerMap.remove(eventHandler);
    }

    public void registerMetaChatSceneEventHandler(IMetachatSceneEventHandler eventHandler) {
        metaChatSceneEventHandlerMap.put(eventHandler, 0);
    }

    public void unregisterMetaChatSceneEventHandler(IMetachatSceneEventHandler eventHandler) {
        metaChatSceneEventHandlerMap.remove(eventHandler);
    }

    public boolean getScenes() {
        return metaChatService.getScenes() == Constants.ERR_OK;
    }

    public boolean isSceneDownloaded(MetachatSceneInfo sceneInfo) {
        return metaChatService.isSceneDownloaded(sceneInfo.mSceneId) > 0;
    }

    public boolean downloadScene(MetachatSceneInfo sceneInfo) {
        return metaChatService.downloadScene(sceneInfo.mSceneId) == Constants.ERR_OK;
    }

    public void prepareScene(MetachatSceneInfo sceneInfo, MetachatUserAvatarConfig avatarConfig) {
        this.sceneInfo = sceneInfo;
        this.avatarConfig = avatarConfig;
    }

    public boolean createAndEnterScene(String roomName) {
        this.roomName = roomName;
        if (metaChatScene == null) {
            metaChatScene = metaChatService.createScene(this.roomName, this);
        }
        return metaChatScene.enterScene(sceneInfo, avatarConfig) == Constants.ERR_OK;
    }

    public boolean updateRole(int role) {
        int ret = Constants.ERR_OK;
        ret += rtcEngine.updateChannelMediaOptions(new ChannelMediaOptions() {{
            publishAudioTrack = true;
            autoSubscribeAudio = true;
            clientRoleType = role;
        }});
        avatarConfig.mLocalVisible = role == Constants.CLIENT_ROLE_BROADCASTER;
        ret += metaChatScene.updateLocalAvatarConfig(avatarConfig);
        return ret == Constants.ERR_OK;
    }

    public boolean enableLocalAudio(boolean enabled) {
        return rtcEngine.enableLocalAudio(enabled) == Constants.ERR_OK;
    }

    public boolean setDefaultAudioRoutetoSpeakerphone(boolean enabled) {
        return rtcEngine.setDefaultAudioRoutetoSpeakerphone(enabled) == Constants.ERR_OK;
    }

    public boolean leaveAndReleaseScene() {
        int ret = Constants.ERR_OK;
        if (metaChatScene != null) {
            ret += rtcEngine.leaveChannel();
            ret += metaChatScene.leaveScene();
            ret += metaChatScene.release();
            metaChatScene = null;
        }
        return ret == Constants.ERR_OK;
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        for (IMetachatEventHandler handler : metaChatEventHandlerMap.keySet()) {
            handler.onConnectionStateChanged(state, reason);
        }
    }

    @Override
    public void onRequestToken() {
        for (IMetachatEventHandler handler : metaChatEventHandlerMap.keySet()) {
            handler.onRequestToken();
        }
    }

    @Override
    public void onGetScenesResult(MetachatSceneInfo[] scenes, int errorCode) {
        for (IMetachatEventHandler handler : metaChatEventHandlerMap.keySet()) {
            handler.onGetScenesResult(scenes, errorCode);
        }
    }

    @Override
    public void onDownloadSceneProgress(MetachatSceneInfo sceneInfo, int progress, int state) {
        for (IMetachatEventHandler handler : metaChatEventHandlerMap.keySet()) {
            handler.onDownloadSceneProgress(sceneInfo, progress, state);
        }
    }

    @Override
    public void onEnterSceneResult(int errorCode) {
        if (errorCode == 0) {
            rtcEngine.joinChannel(
                    KeyCenter.RTC_TOKEN, roomName, KeyCenter.RTC_UID,
                    new ChannelMediaOptions() {{
                        publishAudioTrack = true;
                        autoSubscribeAudio = true;
                        clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                    }});
            // audio的mute状态交给ILocalSpatialAudioEngine统一管理
            rtcEngine.muteAllRemoteAudioStreams(true);
        }
        for (IMetachatSceneEventHandler handler : metaChatSceneEventHandlerMap.keySet()) {
            handler.onEnterSceneResult(errorCode);
        }
    }

    @Override
    public void onLeaveSceneResult(int errorCode) {
        for (IMetachatSceneEventHandler handler : metaChatSceneEventHandlerMap.keySet()) {
            handler.onLeaveSceneResult(errorCode);
        }
    }

    @Override
    public void onRecvMessageFromScene(byte[] message) {
        for (IMetachatSceneEventHandler handler : metaChatSceneEventHandlerMap.keySet()) {
            handler.onRecvMessageFromScene(message);
        }
    }

    @Override
    public void onUserPositionChanged(String uid, MetachatUserPositionInfo posInfo) {
        Log.d(TAG, String.format("onUserPositionChanged %s", uid));
        try {
            int userId = Integer.parseInt(uid);
            if (KeyCenter.RTC_UID == userId) {
                spatialAudioEngine.updateSelfPosition(
                        posInfo.mPosition, posInfo.mForward, posInfo.mRight, posInfo.mUp
                );
            } else {
                spatialAudioEngine.updateRemotePosition(userId, new RemoteVoicePositionInfo() {{
                    position = posInfo.mPosition;
                    forward = posInfo.mForward;
                }});
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        for (IMetachatSceneEventHandler handler : metaChatSceneEventHandlerMap.keySet()) {
            handler.onUserPositionChanged(uid, posInfo);
        }
    }

}
