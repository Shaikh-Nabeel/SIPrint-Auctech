//package com.auctech.siprint.initials.activity
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import android.os.Build
//import android.widget.Toast
//
//class ConnectivityReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
////        val connectivityManager =
////            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//            val network = connectivityManager.activeNetwork
//            if(network == null){
//                openNoInternetActivity(context)
//            }
//            val capabilities = connectivityManager.getNetworkCapabilities(network)
//            if(capabilities == null){
//                openNoInternetActivity(context)
//            }else if(!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)){
//                openNoInternetActivity(context)
//            }
//    }
//
////    private fun openNoInternetActivity(context: Context) {
////        val newIntent = Intent(context, YourActivity::class.java)
////        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
////        context.startActivity(newIntent)
////    }
//}
