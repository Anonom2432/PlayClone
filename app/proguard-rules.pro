# Add project specific ProGuard rules here.
-keep class com.example.playclone.data.model.** { *; }
-keep class com.google.firebase.** { *; }
-keep class androidx.compose.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
