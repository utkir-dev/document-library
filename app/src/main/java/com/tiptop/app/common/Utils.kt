package com.tiptop.app.common

class Utils {
    external fun getSharedPrefName(): String
    external fun getDatabaseName(): String
    external fun getUsersPasswordFolder(): String
    external fun getUsersFolder(): String
    external fun getDocumentsFolder(): String
    external fun getDevicesFolder(): String
    external fun getVersionFolder(): String
    external fun getHeadBytesCount(): String
    external fun getImageHeadBytesCount(): String
    external fun getDeletedIdsFolder(): String
    external fun getBlockSpinnerKey1(): String
    external fun getBlockSpinnerKey2(): String
    external fun getBlockSpinnerKey3(): String
    external fun getDefaultBlockCode(): String
    external fun getDefaultBlockImageCode(): String
    external fun getOrderMaskKey(): String
    external fun getBlockCodeKey(): String
    external fun getKeyStr(number:Long): String
    external fun getSpecStr(number:Long): String
    external fun getBlockBuffer(): Int
    external fun getEncryptor(): String
    external fun getSecretKey(): String

    companion object {
        init {
            System.loadLibrary("utils")
        }
    }
}