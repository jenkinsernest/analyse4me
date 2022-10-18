package Util;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import view_model.brains;

/*
@Author Ernest Jenkins

*/
public class Factory implements ViewModelProvider.Factory {
        private Application mApplication;
        private brains.EventHanlder mParam;


        public  Factory(Application application, brains.EventHanlder param) {
            mApplication = application;
            mParam = param;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new brains(mApplication, mParam);
        }

}
