package com.test.recofaciale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewModelFace : ViewModel() {

    private val faceViewState = MutableStateFlow(FaceViewState())

    val state = faceViewState.asStateFlow()

    fun processFaces(faces: List<Face>){
        viewModelScope.launch {
            if (faces.isNotEmpty()) {
                for (element in faces){
                    val leftEyeOpenProbability = element.leftEyeOpenProbability
                    val rightEyeOpenProbability = element.rightEyeOpenProbability
                    val smilingProbability = element.smilingProbability

                    //Smiling Face
                    if((smilingProbability ?: 0f) > 0.3f) {
                        faceViewState.update {
                            it.copy(
                                isSmiling = true
                            )
                        }
                    }

                    //Eyes are open
                    if((leftEyeOpenProbability ?: 0F) > 0.9F && (rightEyeOpenProbability ?: 0F) > 0.9F
                    ){
                        faceViewState.update {
                            it.copy(
                                areEyesOpen = true
                            )
                        }
                    }

                    //Blinking face
                    if(((leftEyeOpenProbability ?: 0F) < 0.4 && (leftEyeOpenProbability != 0f)) && ((rightEyeOpenProbability ?: 0F) < 0.4F && (leftEyeOpenProbability != 0f))
                    ){
                        faceViewState.update {
                            it.copy(
                                isBlinking = true
                            )
                        }
                    }
                }
            }
        }
    }
}

data class FaceViewState(
    val isBlinking:Boolean = false,
    val areEyesOpen:Boolean = false,
    val isSmiling:Boolean = false
)