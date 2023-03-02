package com.lyfebloc.solanakit.sample

import android.app.Application
import com.lyfebloc.hdwalletkit.Mnemonic
import com.lyfebloc.solanakit.Signer
import com.lyfebloc.solanakit.SolanaKit

class App : Application() {

    lateinit var solanaKit: SolanaKit
    lateinit var signer: Signer

    fun init() {
        assignKitClasses()
    }

    private fun assignKitClasses() {
        val words = Configuration.defaultsWords.split(" ")
        val seed = Mnemonic().toSeed(words, "")
        val address = Signer.address(seed)

        solanaKit = SolanaKit.getInstance(
            instance, address,
            Configuration.rpcSource, Configuration.walletId, true
        )

        signer = Signer.getInstance(seed)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        this.init()
    }

    companion object {
        lateinit var instance: App
            private set
    }

}
