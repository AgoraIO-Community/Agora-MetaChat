package io.agora.metachat.example.dialog

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.GridItem
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import io.agora.metachat.example.R
import io.agora.metachat.example.adapter.TipsAdapter
import io.agora.metachat.example.adapter.UserAdapter
import io.agora.metachat.example.databinding.ProgressBarBinding
import io.agora.metachat.example.databinding.TipsDialogBinding

class CustomDialog {

    companion object {

        data class AvatarGridItem(
            val url: String,
        ) : GridItem {
            override val title: String
                get() = ""

            override fun configureTitle(textView: TextView) {
                textView.visibility = View.GONE
            }

            override fun populateIcon(imageView: ImageView) {
                imageView.load(url) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
            }
        }

        @JvmStatic
        fun showAvatarPicker(
            context: Context,
            selection: ((CharSequence) -> Unit)?,
            positive: ((MaterialDialog) -> Unit)?,
            negative: ((MaterialDialog) -> Unit)?,
        ): MaterialDialog {
            val items = listOf(
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/2.png"),
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/4.png"),
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/1.png"),
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/3.png"),
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/6.png"),
                AvatarGridItem("https://accpic.sd-rtn.com/pic/test/png/5.png"),
            )
            return MaterialDialog(context).gridItems(
                items,
                customGridWidth = R.integer.md_grid_width
            ) { _, _, item ->
                selection?.invoke(item.url)
            }.show {
                title(text = "????????????")
                positiveButton(text = "??????", click = positive)
                negativeButton(text = "??????", click = negative)
            }
        }

        @JvmStatic
        fun showDownloadingChooser(
            context: Context,
            positive: ((MaterialDialog) -> Unit)?,
            negative: ((MaterialDialog) -> Unit)?,
        ): MaterialDialog {
            return MaterialDialog(context).show {
                title(text = "????????????")
                message(text = "????????????MetaChat???????????????350M?????????")
                positiveButton(text = "????????????", click = positive)
                negativeButton(text = "????????????", click = negative)
            }
        }

        @JvmStatic
        fun showDownloadingProgress(
            context: Context, negative: ((MaterialDialog) -> Unit)?,
        ): MaterialDialog {
            return MaterialDialog(context).show {
                title(text = "?????????")
                message(text = "????????????MetaChat???????????????350M?????????")
                customView(
                    view = ProgressBarBinding.inflate(LayoutInflater.from(context)).root,
                    horizontalPadding = true,
                )
                cancelOnTouchOutside(false)
                negativeButton(text = "??????", click = negative)
            }
        }

        @JvmStatic
        fun <T> getCustomView(dialog: MaterialDialog): T {
            return dialog.getCustomView() as T
        }

        @JvmStatic
        fun showTips(context: Context): MaterialDialog {
            return MaterialDialog(context).show {
                val binding = TipsDialogBinding.inflate(LayoutInflater.from(context))
                binding.title.text = "????????????"
                binding.close.setOnClickListener { dismiss() }
                val space = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 11f,
                    context.resources.displayMetrics
                ).toInt()
                binding.list.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.top = space

                        val position = parent.getChildAdapterPosition(view)
                        // Add top margin only for the first item to avoid double space between items
                        if (position == 0)
                            outRect.top = 0
                    }
                })
                binding.list.adapter = TipsAdapter(
                    arrayOf(
                        "?????????????????????????????????????????????????????????????????????????????????",
                        "????????????????????????????????????????????????????????????",
                        "??????????????????????????????????????????????????????????????????",
                    )
                )
                customView(
                    view = binding.root,
                    horizontalPadding = true,
                )
            }
        }

        @JvmStatic
        fun showUsers(
            context: Context,
            adapter: UserAdapter,
        ): MaterialDialog {
            return MaterialDialog(context).show {
                val binding = TipsDialogBinding.inflate(LayoutInflater.from(context))
                binding.title.text = "????????????"
                binding.close.setOnClickListener { dismiss() }
                val space = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6f,
                    context.resources.displayMetrics
                ).toInt()
                binding.list.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.top = space

                        val position = parent.getChildAdapterPosition(view)
                        // Add top margin only for the first item to avoid double space between items
                        if (position == 0)
                            outRect.top = 0
                    }
                })
                binding.list.adapter = adapter
                customView(
                    view = binding.root,
                    horizontalPadding = true,
                )
            }
        }

    }

}
