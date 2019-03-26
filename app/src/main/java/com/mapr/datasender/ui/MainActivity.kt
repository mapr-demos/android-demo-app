package com.mapr.datasender.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.mapr.datasender.R
import com.mapr.datasender.config.Config
import com.mapr.datasender.photo.PhotoService
import com.mapr.datasender.sensors.SensorsService
import dagger.android.AndroidInjection
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainActivity : Activity() {
    private val REQUEST_TAKE_PHOTO = 1

    @Inject
    lateinit var sensorsService: SensorsService

    @Inject
    lateinit var photoService: PhotoService

    @Inject
    lateinit var context: Context

    var enabled: Boolean = false
    private lateinit var connectButton: Button
    private lateinit var photoButton: Button
    private lateinit var settingsButton: Button
    private var config: Config = Config()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        config = getConfig()

        connectButton = findViewById(R.id.button_connect)
        connectButton.setOnClickListener {
            connectionButtonEventHandler()
        }

        photoButton = findViewById(R.id.button_photo)
        photoButton.isEnabled = false
        photoButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        settingsButton = findViewById(R.id.button_settings)
        settingsButton.setOnClickListener {
            editSettings()
        }

        val exitButton: Button = findViewById(R.id.button_exit)
        exitButton.setOnClickListener {
            System.exit(0)
        }

        if (sensorsService.connected)
            setConnected()
    }

    fun connectionButtonEventHandler() {
        if (enabled) {
            setDisconnected()
            sensorsService.stop()
        } else {
            requireLocationPermissionIfNecessary()
            setConnected()
            sensorsService.start(config, this::errorHandle)
        }
    }

    private fun setConnected() {
        enabled = true
        connectButton.text = resources.getString(R.string.button_disconnect)
        settingsButton.isEnabled = false
        photoButton.isEnabled = true
    }

    private fun setDisconnected() {
        enabled = false
        connectButton.text = resources.getString(R.string.button_connect)
        settingsButton.isEnabled = true
        photoButton.isEnabled = false
    }

    private var currentFile: File? = null

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.mapr.datasender",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }.apply {
                    currentFile = photoFile
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            currentFile!!.exists().also {
                photoService.sendPhoto(currentFile!!.absolutePath, config, this::errorHandle)
            }
        }
    }

    private fun requireLocationPermissionIfNecessary() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PackageManager.PERMISSION_GRANTED
            )
    }

    private fun errorHandle(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

        setDisconnected()
        sensorsService.stop()
    }

    private fun editSettings() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setTitle("Settings")
        val dialogLayout = inflater.inflate(R.layout.edit_settings, null)

        val host = dialogLayout.findViewById<TextView>(R.id.text_host)
        host.text = config.host

        val port = dialogLayout.findViewById<TextView>(R.id.digital_port)
        port.text = config.port.toString()

        val username = dialogLayout.findViewById<TextView>(R.id.text_username)
        username.text = config.username

        val password = dialogLayout.findViewById<TextView>(R.id.text_password)
        password.text = config.password

        builder.setView(dialogLayout)
        builder.setPositiveButton(android.R.string.yes) { dialogInterface, i ->
            run {
                config = Config(
                    host.text.toString(),
                    port.text.toString().toInt(),
                    username.text.toString(),
                    password.text.toString()
                )
                saveConfig(config)
                Toast.makeText(
                    applicationContext,
                    "Saved", Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(
                applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }

    private fun getConfig(): Config {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        val defaultHost = resources.getString(R.string.default_host)
        val host = sharedPref.getString(getString(R.string.key_host), defaultHost)

        val defaultPort = resources.getString(R.string.default_port).toInt()
        val port = sharedPref.getInt(getString(R.string.key_port), defaultPort)

        val defaultUsername = resources.getString(R.string.default_username)
        val username = sharedPref.getString(getString(R.string.key_username), defaultUsername)

        val defaultPassword = resources.getString(R.string.default_password)
        val password = sharedPref.getString(getString(R.string.key_password), defaultPassword)

        return Config(host, port, username, password)
    }

    private fun saveConfig(config: Config) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.key_host), config.host)
            putInt(getString(R.string.key_port), config.port)
            putString(getString(R.string.key_username), config.username)
            putString(getString(R.string.key_password), config.password)
            apply()
        }
    }

}