package xyz.mechenbier.circuittester

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

const val KEY_PREF_SHOW_STATE_TEXT = "preference_show_text_state"
const val KEY_PREF_SOUND_WHEN_POWERED = "preference_sound_when_powered"
const val KEY_PREF_SOUND_MUTE = "preference_sound_mute"
const val KEY_PREF_SEND_FEEDBACK = "preference_send_feedback"
const val KEY_PREF_PRIVACY_POLICY = "preference_privacy_policy"
const val KEY_PREF_SHARED_RATING_KEY_NAME = "xyz.mechenbier.circuittester.preferences.apprating"
const val KEY_PREF_RATING_DONT_SHOW_RATING_PROMPT = "preference_dont_show_rating_prompt"
const val KEY_PREF_RATING_LAUNCH_COUNT = "launch_count"
const val KEY_PREF_RATING_DATE_FIRST_LAUNCH = "date_first_launch"

class SettingsActivity :  AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
}