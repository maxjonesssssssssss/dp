#include <jni.h>
#include <stdio.h>
#include "ArithmeticDemo.h"

JNIEXPORT jint JNICALL Java_ArithmeticDemo_add(JNIEnv *env, jobject obj, jint a, jint b) {
    return a + b;
}

JNIEXPORT jint JNICALL Java_ArithmeticDemo_subtract(JNIEnv *env, jobject obj, jint a, jint b) {
    return a - b;
}

JNIEXPORT jint JNICALL Java_ArithmeticDemo_multiply(JNIEnv *env, jobject obj, jint a, jint b) {
    return a * b;
}

JNIEXPORT jfloat JNICALL Java_ArithmeticDemo_divide(JNIEnv *env, jobject obj, jint a, jint b) {
    if (b == 0) {
        printf("Error: Division by zero!\n");
        return 0;
    }
    return (float)a / b;
}
