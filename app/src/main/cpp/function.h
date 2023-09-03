
#ifndef NDKEXAPLE_FUNCTION_H
#define NDKEXAPLE_FUNCTION_H

#define Text string
#define repeat(n) for(int i = 0; i < n; i++)
#define nativeScope extern "C"
#define fun(ClassName, Type, MethodName, ...)  JNIEXPORT Type JNICALL Java_com_tiptop_presentation_##ClassName##_##MethodName(JNIEnv* env, jobject o, ## __VA_ARGS__)


#endif //NDKEXAPLE_FUNCTION_H
