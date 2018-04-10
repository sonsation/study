// IRemoteServiceCallback.aidl
package com.example.administrator.study_jh;

// Declare any non-default types here with import statements

oneway interface IRemoteServiceCallback {
    void valueChanges(long writtenSize);
 }

