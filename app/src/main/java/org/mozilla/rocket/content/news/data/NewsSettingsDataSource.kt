package org.mozilla.rocket.content.news.data

import androidx.lifecycle.LiveData

interface NewsSettingsDataSource {
    fun getSupportLanguages(): LiveData<List<NewsLanguage>>
    fun setSupportLanguages(languages: List<NewsLanguage>)
    fun getUserPreferenceLanguage(): LiveData<NewsLanguage>
    fun setUserPreferenceLanguage(language: NewsLanguage)
    fun getSupportCategories(language: String): LiveData<List<String>>
    fun setSupportCategories(language: String, supportCategories: List<String>)
    fun getUserPreferenceCategories(language: String): LiveData<List<String>>
    fun setUserPreferenceCategories(language: String, userPreferenceCategories: List<String>)
}