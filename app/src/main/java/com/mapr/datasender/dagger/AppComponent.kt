package com.mapr.datasender.dagger

import com.mapr.datasender.DataSenderApplication
import com.mapr.datasender.ui.ActivityBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton
import dagger.android.AndroidInjector

@Singleton
@Component(modules = [AppModule::class, AndroidInjectionModule::class, ActivityBuilder::class])
interface AppComponent : AndroidInjector<DataSenderApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: DataSenderApplication): Builder

        fun build(): AppComponent
    }

    override fun inject(app: DataSenderApplication)
}
