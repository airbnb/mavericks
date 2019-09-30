package com.airbnb.mvrx.news

import android.os.Bundle
import com.airbnb.mvrx.BaseMvRxActivity

class NewsActivity : BaseMvRxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)
    }
}
