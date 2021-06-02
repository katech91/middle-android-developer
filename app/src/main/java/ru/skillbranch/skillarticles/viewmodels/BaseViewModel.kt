package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import java.io.Serializable
import java.lang.IllegalArgumentException

abstract class BaseViewModel<T>(initState: T, private  val savedStateHandle: SavedStateHandle) : ViewModel() where T: VMState {
    val notifications = MutableLiveData<Event<Notify>>()

    val state: MediatorLiveData<T> = MediatorLiveData<T>().apply {
        val restoredState = savedStateHandle.get<Any>("state")?.let {
            if (it is Bundle) initState.fromBundle(it) as? T
            else it as T
        }
        Log.e("BaseViewModel", "handle restore state")
        value = restoredState ?: initState
    }

    //not null current state
    val currentState
        get() = state.value!!

    /***
    * Лямбда-выражение принимает в качестве в качестве аргумента лямбду, в которую предается текущее состояние
    * и она возвращает модифицированное состояние, которое присваивается текущему состоянию
    */
    @UiThread
    protected inline fun updateState(update:(currentState: T) -> T) {
        val updatedState: T = update(currentState)
        state.value = updatedState
    }

    @UiThread
    protected fun notify(content: Notify) {
        notifications.value = Event(content)
    }

    /***
    * более компактная форма записи observe принимает последним аргументом лямбда выражение,
    * обрабатывающее изменение текущего состояния
    */
    fun observeState(owner: LifecycleOwner, onChanged: (newState: T) -> Unit) {
        state.observe(owner, Observer { onChanged(it!!) })
    }

    /***
     * вспомогательная фкнкуия, помогающая наблюдать за изменениями  части стейта ViewModel
     */
    fun <D> observeSubState(owner: LifecycleOwner, transform: (T) -> D, onChanged: (substate: D) -> Unit) {
        state
            .map(transform)  //трансформируем весь стейт в необходимую модель substate
            .distinctUntilChanged() //отфильтровываем и пропускаем дальше только если значение изменилось
            .observe(owner, Observer { onChanged(it!!) })
    }

    /***
    * более компактная форма записи observe вызывает лямбда выражение - обработчик только в том случае,
    * если сообщение не было уже обработано, реализует данное поведение благодаря EventObserver
    */
    fun observeNotifications(owner: LifecycleOwner, onNotify: (notification: Notify)->Unit){
        notifications.observe(owner, EventObserver { onNotify(it) })
    }

    protected fun <S> subscribeOnDataSource(
            source: LiveData<S>,
            onChanged: (newValue: S, currentState: T) -> T?
    ){
        state.addSource(source){
            state.value = onChanged(it, currentState) ?:  return@addSource
        }
    }

    /***
     * Сохранение стейта в bundle
     */
    fun saveState(){
        savedStateHandle.set("state", currentState)
    }

    /***
     * Восстановление стейта из bundle  после смерти процесса
     */
//    fun restoreState(){
//        val restoredState = savedStateHandle.get<T>("state")
//        Log.e("BaseVM", "restoredState: $restoredState")
//        restoredState ?: return
//        state.value = restoredState
//    }
}

class ViewModelFactory(owner: SavedStateRegistryOwner, private val params: String):
    AbstractSavedStateViewModelFactory(owner, bundleOf()) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            return ArticleViewModel(params, handle) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}

class Event<out E>(private val content: E) {
    var hasBeenHandled = false

    fun  getContentIfNotHandled(): E?{
        return if (hasBeenHandled) null
        else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): E = content
}

class EventObserver<E>(private val onEventUnhandledContent: (E) -> Unit): Observer<Event<E>>{
    //в качестве аргумента принимает лямбда выражение - обработчик, в который передается
    // необработанное ранее событие, получаемое в реализации метода Observer'а OnChanged
    override fun onChanged(event: Event<E>?) {
        //если есть необработанное сообщение (контент), передай в лямбду в качестве аргумента
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}

sealed class Notify(val message: String) {
    data class TextMessage(val msg: String): Notify(msg)

    data class ActionMessage(
            val msg: String,
            val actionLabel: String,
            val actionHandler: (()->Unit)?
    ): Notify(msg)

    data class ErrorMessage(
            val msg: String,
            val errLabel: String,
            val errHandler: (()->Unit)?
    ): Notify(msg)
}

public interface VMState: Serializable{
    fun toBundle(): Bundle?
    fun fromBundle(bundle: Bundle): VMState?

}