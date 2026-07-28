package com.example.wearfitness.shared.presentation

import com.example.wearfitness.shared.model.FitnessData

data class FitnessUiState (
    val fitnessData: FitnessData = FitnessData(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
