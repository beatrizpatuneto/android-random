#include "org_devtcg_demo_jnitest_NativeMD5.h"

#include <stdlib.h>
#include <string.h>
#include "md5.h"

/*****************************************************************************/

JNIEXPORT jbyteArray JNICALL Java_org_devtcg_demo_jnitest_NativeMD5_digestStream
  (JNIEnv *env, jclass c, jobject in)
{
	jclass inc;
	jmethodID inRead;
	jbyteArray b;
	jbyteArray ret;
	MD5_CTX ctx;
	unsigned char digest[16];
	jint n;

	inc = (*env)->GetObjectClass(env, in);

	if ((inRead = (*env)->GetMethodID(env, inc, "read", "([B)I")) == 0)
		return NULL;

	if ((b = (*env)->NewByteArray(env, 1024)) == NULL)
		return NULL;

//	MD5Init(&ctx);
	memset(digest, 0, sizeof(digest));

	while (JNI_TRUE)
	{
		jbyte *belem;

		n = (*env)->CallIntMethod(env, in, inRead, b);

		if ((*env)->ExceptionOccurred(env))
			goto catch_exception;

		/* EOF */
		if (n < 0)
			break;

		/* Hmm... */
		if (n == 0)
			continue;

		belem = (*env)->GetByteArrayElements(env, b, NULL);
//		MD5Update(&ctx, (unsigned char *)belem, n);
		(*env)->ReleaseByteArrayElements(env, b, belem, JNI_ABORT);
	}

//	MD5Final(digest, &ctx);

	if ((ret = (*env)->NewByteArray(env, 16)) == NULL)
		return NULL;

	(*env)->SetByteArrayRegion(env, ret, 0, 16, (jbyte *)digest);

	return ret;

catch_exception:
	(*env)->ExceptionClear(env);
	return JNI_FALSE;
}

/*****************************************************************************/

static jbyteArray digest_stream(JNIEnv *env, FILE *fp)
{
	jbyteArray ret;
	MD5_CTX ctx;
	unsigned char digest[16];
	char buf[1048];
	size_t n;

//	MD5Init(&ctx);
	memset(digest, 0, sizeof(digest));

	while ((n = fread(buf, 1, sizeof(buf), fp)) > 0)
		/* MD5Update(&ctx, buf, n) */;

//	MD5Final(digest, &ctx);

	if (ferror(fp))
		return NULL;

	if ((ret = (*env)->NewByteArray(env, 16)) == NULL)
		return NULL;

	(*env)->SetByteArrayRegion(env, ret, 0, 16, (jbyte *)digest);

	return ret;
}

static jbyteArray digest_file(JNIEnv *env, const char *file)
{
	FILE *fp;
	jbyteArray ret;

	if ((fp = fopen(file, "r")) != NULL)
	{
		ret = digest_stream(env, fp);
		fclose(fp);
	}

	return ret;
}

JNIEXPORT jbyteArray JNICALL Java_org_devtcg_demo_jnitest_NativeMD5_digestFile
  (JNIEnv *env, jclass c, jstring in)
{
	const char *inname;
	jbyteArray ret = NULL;

	if ((inname = (*env)->GetStringUTFChars(env, in, NULL)) != NULL)
	{
		ret = digest_file(env, inname);
		(*env)->ReleaseStringUTFChars(env, in, inname);
	}

	return ret;
}
