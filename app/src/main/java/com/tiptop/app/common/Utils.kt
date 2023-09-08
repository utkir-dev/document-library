package com.tiptop.app.common

class Utils {
    external fun getSharedPrefName(): String
    external fun getDatabaseName(): String
    external fun getUsersPasswordFolder(): String
    external fun getUsersFolder(): String
    external fun getDocumentsFolder(): String
    external fun getDevicesFolder(): String
    external fun getFildesFolder(): String
    external fun getHeadBytesCount(): String
    external fun getImageHeadBytesCount(): String
    external fun getDeletedIdsFolder(): String
    external fun getBlockSpinnerKey1(): String
    external fun getBlockSpinnerKey2(): String
    external fun getBlockSpinnerKey3(): String
    external fun getDefaultBlockCode(): String
    external fun getDefaultBlockImageCode(): String
    external fun getBlockCodeKey(): String

    companion object {
        init {
            System.loadLibrary("utils")
        }
    }
}