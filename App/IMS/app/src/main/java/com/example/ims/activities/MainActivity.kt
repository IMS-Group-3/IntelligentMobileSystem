package com.example.ims.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ims.*
import com.example.ims.databinding.ActivityMainBinding
import com.example.ims.fragments.MapsFragment
import createNotificationChannel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPermissions()
        replaceFragment(HomeFragment())

        createNotificationChannel(this)

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment())
                R.id.map -> replaceFragment(MapsFragment())
                R.id.controller -> replaceFragment(ControlFragment())
                R.id.history -> replaceFragment(HistoryFragment())

                else -> {

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_fragment, fragment)
        fragmentTransaction.commit()
    }

    override fun onStart() {
        super.onStart()
       // showBluetoothDialog()

    }
    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1-> {
                if (grantResults.isEmpty() || grantResults.first() != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(
                        applicationContext,
                        "getString(R.string.permission_required)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            3-> {
                if (grantResults.isEmpty() || grantResults.first() != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(
                        applicationContext,
                        "gggghghg",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }*/
    private fun showBluetoothDialog(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN )
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED)
                    ){
            if(!bluetoothAdapter.isEnabled){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode != Activity.RESULT_OK){
                replaceFragment(HistoryFragment())
            }
        }
    private fun setPermissions(){
        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val bluetoothScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)

            if (bluetoothScanPermission != PackageManager.PERMISSION_GRANTED){ makePermissionRequests(1) }
            if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED){ makePermissionRequests(2) }        }

        val accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED){ makePermissionRequests(3) }
        if (accessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){ makePermissionRequests(4) }*/
        makePermissionRequests(1)
    }

    private fun makePermissionRequests(permissionCode: Int) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            /*when(permissionCode){
            1->{ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN),permissionCode)}
            2->{ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),permissionCode)}
            }*/
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),permissionCode)
        }else{
            ActivityCompat.requestPermissions(this, arrayOf(

                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),permissionCode)
        }
        /*when(permissionCode){
            3->{ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),permissionCode)}
            4->{ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),permissionCode)}

        }*/
    }
}