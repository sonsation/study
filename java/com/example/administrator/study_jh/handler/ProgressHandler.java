package com.example.administrator.study_jh.handler;

public class ProgressHandler {

    static long partialFileSize;
    static long TotalFileSize;
    static String fileName;

    static int progressCount;
    static int totalFileCount;
    static long fileSize;

    //for ZipService
    public ProgressHandler (String fileName, long partialFileSize,long TotalFileSize){
        this.fileName = fileName;
        this.partialFileSize = partialFileSize;
        this.TotalFileSize = TotalFileSize;
    }

    //for CopyService, RemoveService
    public ProgressHandler (String fileName, long fileSize, int progressCount, int totalFileCount ){
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.progressCount = progressCount;
        this.totalFileCount = totalFileCount;
    }

    public static long getPartialFileSize(){
        return partialFileSize;
    }

    public static long getTotalFileSize(){
        return TotalFileSize;
    }

    public static String getFileName(){
        return fileName;
    }

    public static int getProgressCount() { return progressCount; }

    public static int getTotalFileCount() { return totalFileCount; }

    public static long getFileSize() { return fileSize; }

}
