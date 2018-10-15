package xyz.mechenbier.circuittester

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

const val KEY_PREF_SHOW_STATE_TEXT = "preference_show_text_state"
const val KEY_PREF_SOUND_WHEN_POWERED = "preference_sound_when_powered"
const val KEY_PREF_SOUND_MUTE = "preference_sound_mute"
const val KEY_PREF_SEND_FEEDBACK = "preference_send_feedback"
const val KEY_PREF_PRIVACY_POLICY = "preference_privacy_policy"
const val KEY_PREF_SHARED_RATING_KEY_NAME = "xyz.mechenbier.circuittester.preferences.apprating"
const val KEY_PREF_RATING_DONT_SHOW_RATING_PROMPT = "preference_dont_show_rating_prompt"
const val KEY_PREF_RATING_LAUNCH_COUNT = "launch_count"
const val KEY_PREF_RATING_DATE_FIRST_LAUNCH = "date_first_launch"

class SettingsFragment : PreferenceFragmentCompat()  {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val packageManager = activity!!.packageManager

        val sendEmailIntent = Intent(Intent.ACTION_SENDTO)
        val uriText = "mailto:" + Uri.encode(getString(R.string.feedback_email_address)) + "?subject=" + Uri.encode(getString(R.string.app_name) + " " + getString(R.string.launch_feedback_text))
        sendEmailIntent.data = Uri.parse(uriText)

        val openPrivacyPolicyIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url)))

        val preference = this.preferenceScreen.findPreference(KEY_PREF_SEND_FEEDBACK)
        if (sendEmailIntent.resolveActivity(packageManager) == null){
            this.preferenceScreen.removePreference(preference)
        } else {
            preference.intent = sendEmailIntent
        }

        if (openPrivacyPolicyIntent.resolveActivity(packageManager) == null){
            val preference = this.preferenceScreen.findPreference(KEY_PREF_PRIVACY_POLICY)
            this.preferenceScreen.removePreference(preference)
        }
    }
}