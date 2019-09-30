package com.airbnb.mvrx.news.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil.api.load
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.OnViewRecycled
import com.airbnb.mvrx.news.R
import com.airbnb.mvrx.news.models.Article

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ArticleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val title: TextView
    private val content: TextView
    private val image: ImageView

    init {
        inflate(context, R.layout.item_article, this)
        title = findViewById(R.id.title)
        content = findViewById(R.id.content)
        image = findViewById(R.id.image)
    }

    @ModelProp
    fun setArticle(article: Article) {
        title.text = article.title
        content.text = article.content ?: context.getString(R.string.noContentText)
        image.load(article.urlToImage) {
            crossfade(true)
        }
    }

    @CallbackProp
    fun setOnArticleClick(onClick: OnClickListener?) {
        onClick?.let {
            setOnClickListener(it)
        }
    }

    @OnViewRecycled
    fun cleanup() {
        setOnClickListener(null)
    }
}