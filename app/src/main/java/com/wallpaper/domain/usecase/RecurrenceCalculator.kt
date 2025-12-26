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
        
        // Si des jours spécifiques sont sélectionnés, utiliser la logique hebdomadaire
        val hasSpecificDays = rule.daysOfWeek.isNotEmpty()
        
        return when {
            hasSpecificDays -> {
                // Convertir Calendar.DAY_OF_WEEK (1=Sunday, 2=Monday...) vers notre format (1=Monday, 7=Sunday)
                val dayOfWeek = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1
                val dayMatches = rule.daysOfWeek.contains(dayOfWeek)
                
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
            rule.type == RecurrenceType.DAILY -> {
                // Vérifier les heures si définies
                if (rule.startHour != null && rule.endHour != null) {
                    currentHour >= rule.startHour && currentHour < rule.endHour
                } else {
                    true
                }
            }
            rule.type == RecurrenceType.WEEKLY -> {
                // Tous les jours de la semaine (pas de jours spécifiques)
                if (rule.startHour != null && rule.endHour != null) {
                    currentHour >= rule.startHour && currentHour < rule.endHour
                } else {
                    true
                }
            }
            rule.type == RecurrenceType.MONTHLY -> {
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
            else -> false
        }
    }
}

