package me.cyber.nukleos

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
open class BaseFragment<P: BasePresenter<BaseView>> : Fragment(), BaseView {

    open var presenter: P? = null

    fun attachPresenter(presenter: P) {
        this.presenter = presenter
        getPermissionsWithPermissionCheck()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter?.create()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            presenter?.start()
        } else {
            presenter?.destroy()
        }
    }


    @NeedsPermission(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getPermissions(){

    }

}