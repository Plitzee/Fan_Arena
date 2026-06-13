package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FanArenaRepository
import com.example.data.NotificationItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: FanArenaRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Thêm unreadCount để khớp với UI
    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            repository.loggedInUser.collect { user ->
                user?.let {
                    repository.getNotificationsFlow(it.email).collect { list ->
                        _notifications.value = list
                    }
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            val user = repository.getLoggedInUserDirect()
            user?.let {
                repository.markNotificationAsRead(it.email, id)
            }
        }
    }
}
