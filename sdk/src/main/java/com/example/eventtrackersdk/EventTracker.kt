package com.example.eventtrackersdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.children

object EventTracker {

    private lateinit var application: Application
    private val CLICK_LISTENER_TAG = R.id.click_listener_tag

    fun initialize(context: Context) {
        application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            logEvent("navigate", "Navigated to ${activity::class.java.simpleName}")
            attachGlobalLayoutListener(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            logEvent("navigate", "Resumed ${activity::class.java.simpleName}")
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}

        private fun attachGlobalLayoutListener(activity: Activity) {
            val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
            rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    traverseAndAttachListeners(rootView)
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }

        private fun traverseAndAttachListeners(view: View) {
            if (view is ViewGroup) {
                for (child in view.children) {
                    traverseAndAttachListeners(child)
                }
            } else if (view.isClickable && view.getTag(CLICK_LISTENER_TAG) == null) {
                attachClickListener(view)
            }
        }

        private fun attachClickListener(view: View) {
            val originalClickListener = getOriginalClickListener(view)

            view.setOnClickListener { v ->
                // Log the click event immediately
                logEvent("click", "Clicked on view with ID ${v.id} (${v::class.java.simpleName})")

                // Call the original click listener if it exists
                originalClickListener?.onClick(v)
            }

            // Tag the view to prevent re-attaching the listener
            view.setTag(CLICK_LISTENER_TAG, true)
        }

        private fun getOriginalClickListener(view: View): View.OnClickListener? {
            return try {
                val listenerInfoField = View::class.java.getDeclaredField("mListenerInfo")
                listenerInfoField.isAccessible = true
                val listenerInfo = listenerInfoField.get(view)

                val clickListenerField = listenerInfo.javaClass.getDeclaredField("mOnClickListener")
                clickListenerField.isAccessible = true
                clickListenerField.get(listenerInfo) as? View.OnClickListener
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun logEvent(eventType: String, details: String) {
        val timestamp = System.currentTimeMillis()
        val osVersion = Build.VERSION.SDK_INT
        val logMessage = "Time: $timestamp, EventType: $eventType, Details: $details, OS: $osVersion"

        Log.d("EventTracker", logMessage)

        // Store the event in SharedPreferences
        val sharedPreferences = application.getSharedPreferences("EventTrackerPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("event_$timestamp", logMessage)
        editor.apply()
    }
}
