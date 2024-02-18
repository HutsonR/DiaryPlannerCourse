package com.easyflow.diarycourse.core

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.easyflow.diarycourse.R

@JvmOverloads
fun NavController.navigateWithAnimation(
    @IdRes
    id: Int,
    bundle: Bundle? = null,
    navBuilder: NavOptions.Builder? = null
) {
    fun NavOptions.Builder.setBaseAnimations(): NavOptions.Builder =
        this.setEnterAnim(R.anim.zoom_in)
            .setExitAnim(R.anim.zoom_out).setPopEnterAnim(R.anim.zoom_in)
            .setPopExitAnim(R.anim.zoom_out)

    val builder = navBuilder ?: NavOptions.Builder()
    val options = builder.setBaseAnimations()
    navigate(id, bundle, options.build())
}