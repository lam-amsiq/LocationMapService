package lam.com.locationmapservice.lib.controllers.location

import android.location.Location
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class LocationObserver(private val onNextAction: Consumer<Location>? = null, private val onErrorAction: Consumer<Throwable>? = null, private val onCompleteAction: Runnable? = null, private val onSubscribeAction: Consumer<Disposable>? = null): io.reactivex.Observer<Location?> {
    override fun onNext(location: Location) {
        onNextAction?.accept(location)
    }

    override fun onError(error: Throwable) {
        onErrorAction?.accept(error)
    }

    override fun onComplete() {
        onCompleteAction?.run()
    }

    override fun onSubscribe(disposable: Disposable) {
        onSubscribeAction?.accept(disposable)
    }
}