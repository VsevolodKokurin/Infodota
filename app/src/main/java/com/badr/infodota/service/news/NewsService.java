package com.badr.infodota.service.news;

import android.content.Context;
import android.util.Pair;

import com.badr.infodota.InitializingBean;
import com.badr.infodota.api.news.NewsItem;

import java.util.List;

/**
 * User: Histler
 * Date: 21.04.14
 */
public interface NewsService extends InitializingBean {
    Pair<List<NewsItem>, String> getNews(Context context, Long fromDate);
}
