package com.example.tugasakhir

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tugasakhir.presentation.auth.AuthRepository
import com.example.tugasakhir.presentation.auth.AuthViewModel
import com.example.tugasakhir.presentation.dicom.DicomFirebaseRepository
import com.example.tugasakhir.presentation.dicom.DicomViewModel
import com.example.tugasakhir.presentation.dicom.type.axial.AxialRepository
import com.example.tugasakhir.presentation.dicom.type.axial.AxialViewModel
import com.example.tugasakhir.presentation.dicom.type.coronal.CoronalRepository
import com.example.tugasakhir.presentation.dicom.type.coronal.CoronalViewModel
import com.example.tugasakhir.presentation.dicom.type.sagital.SagitalRepository
import com.example.tugasakhir.presentation.dicom.type.sagital.SagitalViewModel
import com.example.tugasakhir.presentation.edit.OrientationType
import com.example.tugasakhir.presentation.home.HomeRepository
import com.example.tugasakhir.presentation.home.HomeViewModel

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val homeRepository: HomeRepository,
    private val firebaseRepository: DicomFirebaseRepository,
    private val axialRepository: AxialRepository,
    private val coronalRepository: CoronalRepository,
    private val sagitalRepository: SagitalRepository,
    private val orientationType: OrientationType? = null
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(homeRepository) as T
            }
            modelClass.isAssignableFrom(DicomViewModel::class.java) -> {
                DicomViewModel(firebaseRepository) as T
            }
            modelClass.isAssignableFrom(AxialViewModel::class.java) -> {
                requireNotNull(orientationType) { "orientationType is required for AxialViewModel" }
                AxialViewModel(axialRepository, orientationType) as T
            }
            modelClass.isAssignableFrom(CoronalViewModel::class.java) -> {
                requireNotNull(orientationType) { "orientationType is required for CoronalViewModel" }
                CoronalViewModel(coronalRepository, orientationType) as T
            }
            modelClass.isAssignableFrom(SagitalViewModel::class.java) -> {
                requireNotNull(orientationType) { "orientationType is required for SagitalViewModel" }
                SagitalViewModel(sagitalRepository, orientationType) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        fun getDefault(context: Context): ViewModelFactory {
            return ViewModelFactory(
                AuthRepository(),
                HomeRepository(context),
                DicomFirebaseRepository(),
                AxialRepository(),
                CoronalRepository(),
                SagitalRepository()
            )
        }

        fun withOrientation(context: Context, orientationType: OrientationType): ViewModelFactory {
            return ViewModelFactory(
                AuthRepository(),
                HomeRepository(context),
                DicomFirebaseRepository(),
                AxialRepository(),
                CoronalRepository(),
                SagitalRepository(),
                orientationType
            )
        }
    }
}
