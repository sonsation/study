// IRemoteService.aidl
package com.example.administrator.study_jh;

import com.example.administrator.study_jh.IRemoteServiceCallback;

interface IRemoteService {
    boolean registerCallback(IRemoteServiceCallback callback);
    boolean unregisterCallback(IRemoteServiceCallback callback);
}

