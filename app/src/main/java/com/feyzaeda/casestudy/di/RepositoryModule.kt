package com.feyzaeda.casestudy.di

import com.feyzaeda.casestudy.datasource.DataSource
import com.feyzaeda.casestudy.repo.PersonRepository
import com.feyzaeda.casestudy.repo.PersonRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideDataSource() = DataSource()

    @Provides
    fun providePersonRepository(dataSource: DataSource): PersonRepository =
        PersonRepositoryImpl(dataSource)

}