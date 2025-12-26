package com.wallpaper.domain.usecase

import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.data.model.RecurrenceType
import kotlinx.datetime.*
import java.util.Calendar

class RecurrenceCalculator {
    
    /**
     * Vérifie si un répertoire est actif selon sa règle de récurrence
     */
    fun isActive(rule: RecurrenceRule, currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())): Boolean {
        if (rule.type == RecurrenceType.NONE) {
            return false
        }
        
        val calendar = Calendar.getInstance()
        calendar.set(currentDateTime.year, currentDateTime.monthNumber - 1, currentDateTime.dayOfMonth, 
                     currentDateTime.hour, currentDateTime.minute)
        
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = currentDateTime.hour
        
        return when (rule.type) {
            RecurrenceType.DAILY -> {
                // Vérifier les heures si définies
                if (rule.startHour != null && rule.endHour != null) {
                    currentHour >= rule.startHour && currentHour < rule.endHour
                } else {
                    true
                }
            }
            RecurrenceType.WEEKLY -> {
                // Convertir Calendar.DAY_OF_WEEK (1=Sunday, 2=Monday...) vers notre format (1=Monday, 7=Sunday)
                val dayOfWeek = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
                val dayMatches = rule.daysOfWeek.isEmpty() || rule.daysOfWeek.contains(dayOfWeek)
                
                if (dayMatches) {
                    // Vérifier les heures si définies
                    if (rule.startHour != null && rule.endHour != null) {
                        currentHour >= rule.startHour && currentHour < rule.endHour
                    } else {
                        true
                    }
                } else {
                    false
                }
            }
            RecurrenceType.MONTHLY -> {
                val dayMatches = rule.dayOfMonth == null || rule.dayOfMonth == currentDateTime.dayOfMonth
                
                if (dayMatches) {
                    // Vérifier les heures si définies
                    if (rule.startHour != null && rule.endHour != null) {
                        currentHour >= rule.startHour && currentHour < rule.endHour
                    } else {
                        true
                    }
                } else {
                    false
                }
            }
            RecurrenceType.NONE -> false
        }
    }
}

