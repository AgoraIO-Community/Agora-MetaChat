//
//  ViewController.swift
//  MetaChatDemo
//
//  Created by 胡润辰 on 2022/4/21.
//

import UIKit
import AgoraRtcKit

class SelSexCell: UIView {
    @IBOutlet weak var selectedBack: UIView!
    @IBOutlet weak var selectedButton: UIButton!
}

protocol SelSexAlertDelegate: NSObjectProtocol {
    func onSelectSex(index: Int)
    
    func onSelectCancel()
}

class SelSexAlert: UIView {
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var selManCell: SelSexCell!
    @IBOutlet weak var selWomanCell: SelSexCell!
    
    public var selIndex: Int = 0
    
    weak var delegate: SelSexAlertDelegate?
        
    @IBAction func selectedAction(sender: UIButton) {
        if sender == selManCell.selectedButton {
            selIndex = 0
            selManCell.selectedBack.isHidden = false
            selWomanCell.selectedBack.isHidden = true
        }else if sender == selWomanCell.selectedButton {
            selIndex = 1
            selManCell.selectedBack.isHidden = true
            selWomanCell.selectedBack.isHidden = false
        }
        
        delegate?.onSelectSex(index: selIndex)
        
        isHidden = true
    }
    
    @IBAction func cancelAction(sender: UIButton) {
        delegate?.onSelectCancel()
        
        isHidden = true
    }
}

class SelAvatarCell: UIView {
    @IBOutlet weak var selectedIcon: UIImageView!
    @IBOutlet weak var selectedButton: UIButton!
}

protocol SelAvatarAlertDelegate: NSObjectProtocol {
    func onSelectAvatar(index: Int)
}

class SelAvatarAlert: UIView {
    @IBOutlet weak var blankButton: UIButton!

    @IBOutlet weak var avatarBoardView: UIView!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var ensureButton: UIButton!
    
    public var selIndex: Int = 0

    weak var delegate: SelAvatarAlertDelegate?
    
    func setUI() {
        avatarBoardView.layer.borderWidth = 1.0
        avatarBoardView.layer.borderColor = UIColor.init(red: 224.0/255.0, green: 216.0/255.0, blue: 203.0/255.0, alpha: 1.0).cgColor
        avatarBoardView.layer.cornerRadius = 4.0
        
        cancelButton.layer.borderWidth = 1.0
        cancelButton.layer.borderColor = UIColor.init(red: 111/255.0, green: 87/255.0, blue: 235/255.0, alpha: 1.0).cgColor
        cancelButton.layer.cornerRadius = 20.0

    }

    
    @IBAction func cancelAction(sender: UIButton) {
        isHidden = true
    }

    @IBAction func selectedAction(sender: UIButton) {
        selIndex = sender.superview?.tag ?? 0;
        
        for subView in avatarBoardView.subviews {
            let avatarCell = subView as! SelAvatarCell
            
            if avatarCell == sender.superview {
                avatarCell.selectedIcon.isHidden = false
            }else {
                avatarCell.selectedIcon.isHidden = true
            }
        }
    }
    
    @IBAction func ensureAction(sender: UIButton) {
        delegate?.onSelectAvatar(index: selIndex)
        
        isHidden = true
    }
}

class MetaChatLoginViewController: UIViewController {
    @IBOutlet weak var selSexAlert: SelSexAlert!
    @IBOutlet weak var selAvatarAlert: SelAvatarAlert!
    
    @IBOutlet weak var selSexLabel: UILabel!
    @IBOutlet weak var selSexIcon: UIImageView!
    
    @IBOutlet weak var avatarImageView: UIImageView!
    @IBOutlet weak var userNameTF: UITextField!
    @IBOutlet weak var errorLabel: UILabel!
    
    @IBOutlet weak var downloadingBack: UIView!
    @IBOutlet weak var downloadingProgress: UIProgressView!
    
    @IBOutlet weak var cancelDownloadButton: UIButton!
    
    private var currentSceneId: Int = 0
    
    var selSex: Int = 0    //0未选择，1男，2女
    
    var selAvatarIndex: Int = 0
    
    var avatarUrlArray = ["https://accpic.sd-rtn.com/pic/test/png/2.png", "https://accpic.sd-rtn.com/pic/test/png/4.png", "https://accpic.sd-rtn.com/pic/test/png/1.png", "https://accpic.sd-rtn.com/pic/test/png/3.png", "https://accpic.sd-rtn.com/pic/test/png/6.png", "https://accpic.sd-rtn.com/pic/test/png/5.png"]
    
    override func viewDidLoad() {
        super.viewDidLoad()

        userNameTF.attributedPlaceholder = NSAttributedString.init(string: "请输入2-10个字符", attributes: [NSAttributedString.Key.foregroundColor : UIColor.init(red: 161.0/255.0, green: 139.0/255.0, blue: 176/255.0, alpha: 1.0)])
        
        selSexAlert.delegate = self
        selAvatarAlert.setUI()
        
        selAvatarAlert.delegate = self
        
        view.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(hideKeyboard)))
    }

    override var shouldAutorotate: Bool {
        return false
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
        
    @objc func hideKeyboard() {
        view.endEditing(true)
    }
    
    @IBAction func selectedSexAction(sender: UIButton) {
        selSexAlert.isHidden = false
        
        selSexIcon.image = UIImage.init(named: "arrow-up")
        view.endEditing(true)
    }
    
    @IBAction func selectedAvatarAction(sender: UIButton) {
        selAvatarAlert.isHidden = false
    }
    
    @IBAction func cancelDownloadHandler(_ sender: Any) {
        downloadingBack.isHidden = true
        MetaChatEngine.sharedEngine.metachatKit?.cancelDownloadScene(currentSceneId)
    }
    
    func checkValid() -> Bool {
        let nameCount = userNameTF.text?.count ?? 0
        if (nameCount < 2) || (nameCount > 10) {
            errorLabel.text = "姓名必须包含2-10个字符"
            return false
        }
        
        if selSex == 0 {
            errorLabel.text = "请选择性别"
            return false
        }
        
        errorLabel.text = nil
        return true
    }
        
    var indicatorView: UIActivityIndicatorView?
    
    @IBAction func enterScene(sender: UIButton) {
        if !checkValid() {
            return
        }
                
        indicatorView = UIActivityIndicatorView.init(frame: view.frame)
        indicatorView?.style = UIActivityIndicatorView.Style.large
        indicatorView?.color = UIColor.white
        view.addSubview(indicatorView!)
        indicatorView?.startAnimating()
        
        MetaChatEngine.sharedEngine.createMetachatKit(userName: userNameTF.text!, avatarUrl: avatarUrlArray[selAvatarIndex], delegate: self)
                        
        MetaChatEngine.sharedEngine.metachatKit?.getScenes()
        
    }
    
    func onSceneReady(_ sceneInfo: AgoraMetachatSceneInfo) {
        let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
        
        DispatchQueue.main.async {
            self.downloadingBack.isHidden = true
            
            guard let sceneViewController = storyBoard.instantiateViewController(withIdentifier: "SceneViewController") as? MetaChatSceneViewController else { return }
            sceneViewController.modalPresentationStyle = .fullScreen
            MetaChatEngine.sharedEngine.createScene(sceneInfo, delegate: sceneViewController)

            self.present(sceneViewController, animated: true)
        }
        

    }
}

extension MetaChatLoginViewController: SelSexAlertDelegate {
    func onSelectCancel() {
        selSexIcon.image = UIImage.init(named: "arrow-down")
    }
    
    func onSelectSex(index: Int) {
        selSex = index + 1
        
        if selSex == 1 {
            selSexLabel.text = "男"
        }else if selSex == 2 {
            selSexLabel.text = "女"
        }
        
        selSexIcon.image = UIImage.init(named: "arrow-down")
    }
}


extension MetaChatLoginViewController: SelAvatarAlertDelegate {
    func onSelectAvatar(index: Int) {
        selAvatarIndex = index
        
        let localImageName = "avatar\(index+1)"
        avatarImageView.image = UIImage.init(named: localImageName)
    }
}

extension MetaChatLoginViewController: AgoraMetachatEventDelegate {
    func onConnectionStateChanged(_ state: AgoraMetachatConnectionStateType, reason: AgoraMetachatConnectionChangedReasonType) {
        if state == .disconnected {
            DispatchQueue.main.async {
                self.indicatorView?.stopAnimating()
                self.indicatorView?.removeFromSuperview()
                self.indicatorView = nil
            }
        }
    }
    
    func onRequestToken() {
        
    }
    
    func onGetScenesResult(_ scenes: NSMutableArray, errorCode: Int) {
        DispatchQueue.main.async {
            self.indicatorView?.stopAnimating()
            self.indicatorView?.removeFromSuperview()
            self.indicatorView = nil
        }
        
        if errorCode != 0 {
            let alertController = UIAlertController.init(title: "get Scenes failed:errorcode:\(errorCode)", message:nil , preferredStyle:.alert)
            
            alertController.addAction(UIAlertAction.init(title: "确定", style: .cancel, handler: nil))

            DispatchQueue.main.async {
                self.present(alertController, animated: true)
            }
            return
        }
        
        if scenes.count == 0 {
            return
        }
        
        let firstScene = scenes.firstObject as! AgoraMetachatSceneInfo
        currentSceneId = firstScene.sceneId
        let metachatKit = MetaChatEngine.sharedEngine.metachatKit
        if metachatKit?.isSceneDownloaded(currentSceneId) != 1 {
            let alertController = UIAlertController.init(title: "下载提示", message: "首次进入MetaChat场景需下载50M数据包", preferredStyle:.alert)
            
            alertController.addAction(UIAlertAction.init(title: "下次再说", style: .cancel, handler: nil))
            alertController.addAction(UIAlertAction.init(title: "立即下载", style: .default, handler: { UIAlertAction in
                metachatKit?.downloadScene(firstScene.sceneId)
                self.downloadingBack.isHidden = false
            }))
            
            DispatchQueue.main.async {
                self.present(alertController, animated: true)
            }
        }else {
            onSceneReady(firstScene)
        }
    }
    
    func onDownloadSceneProgress(_ sceneInfo: AgoraMetachatSceneInfo?, progress: Int, state: AgoraMetachatDownloadStateType) {
        DispatchQueue.main.async {
            self.downloadingProgress.progress = Float(progress)/100.0
        }
        
        if state == .downloaded && sceneInfo != nil {
            onSceneReady(sceneInfo!)
        }
    }
}
