package org.mozilla.rocket.content.di

import android.content.Context
import dagger.Module
import dagger.Provides
import org.mozilla.lite.newspoint.RepositoryNewsPoint
import org.mozilla.lite.partner.NewsItem
import org.mozilla.lite.partner.Repository
import org.mozilla.rocket.content.news.LoadNewsSettingsUseCase
import org.mozilla.rocket.content.news.NewsViewModelFactory
import org.mozilla.rocket.content.news.data.NewsRepository
import org.mozilla.rocket.content.news.data.NewsSettingsLocalDataSource
import org.mozilla.rocket.content.news.data.NewsSettingsRemoteDataSource
import org.mozilla.rocket.content.news.data.NewsSettingsRepository
import java.util.Locale
import javax.inject.Singleton

@Module
object ContentModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideNewsSettingsRemoteDataSource(): NewsSettingsRemoteDataSource {
        return NewsSettingsRemoteDataSource()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNewsSettingsLocalDataSource(context: Context): NewsSettingsLocalDataSource {
        return NewsSettingsLocalDataSource(context)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNewsSettingsRepository(
        newsSettingsRemoteDataSource: NewsSettingsRemoteDataSource,
        newsSettingsLocalDataSource: NewsSettingsLocalDataSource
    ): NewsSettingsRepository {
        return NewsSettingsRepository(newsSettingsRemoteDataSource, newsSettingsLocalDataSource)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideLoadNewsSettingsUseCase(newsSettingsRepository: NewsSettingsRepository): LoadNewsSettingsUseCase =
            LoadNewsSettingsUseCase(newsSettingsRepository)

    @JvmStatic
    @Singleton
    @Provides
    fun provideNewsViewModelFactory(loadNewsSettingsUseCase: LoadNewsSettingsUseCase): NewsViewModelFactory =
            NewsViewModelFactory(loadNewsSettingsUseCase)

    @JvmStatic
    @Singleton
    @Provides
    fun provideNewsRepository(
        context: Context,
        configurations: HashMap<String, String>
    ): Repository<out NewsItem> {
        val url = String.format(
            Locale.getDefault(),
            configurations[NewsRepository.CONFIG_URL] ?: "",
            configurations[NewsRepository.CONFIG_CATEGORY],
            configurations[NewsRepository.CONFIG_LANGUAGE],
            "%d",
            "%d"
        )
        return RepositoryNewsPoint(context, url)
    }
}
