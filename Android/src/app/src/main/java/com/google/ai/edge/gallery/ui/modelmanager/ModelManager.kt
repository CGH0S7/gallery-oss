/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.modelmanager

// import androidx.compose.ui.tooling.preview.Preview
// import com.google.ai.edge.gallery.ui.preview.PreviewModelManagerViewModel
// import com.google.ai.edge.gallery.ui.preview.TASK_TEST1
// import com.google.ai.edge.gallery.ui.theme.GalleryTheme

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.ai.edge.gallery.GalleryTopAppBar
import com.google.ai.edge.gallery.data.AppBarAction
import com.google.ai.edge.gallery.data.AppBarActionType
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.proto.ImportedModel

/** A screen to manage models. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManager(
  task: Task,
  viewModel: ModelManagerViewModel,
  enableAnimation: Boolean,
  navigateUp: () -> Unit,
  onModelClicked: (Model) -> Unit,
  modifier: Modifier = Modifier,
  onBenchmarkClicked: (Model) -> Unit = {},
) {
  // Set title based on the task.
  val title = task.label
  var selectedLocalModelUri by remember { mutableStateOf<android.net.Uri?>(null) }
  var selectedImportedModelInfo by remember { mutableStateOf<ImportedModel?>(null) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showImportingDialog by remember { mutableStateOf(false) }
  val filePickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        selectedLocalModelUri = uri
        showImportDialog = true
      }
    }

  // Handle system's edge swipe.
  BackHandler { navigateUp() }

  Scaffold(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    topBar = {
      GalleryTopAppBar(
        title = title,
        leftAction = AppBarAction(actionType = AppBarActionType.NAVIGATE_UP, actionFn = navigateUp),
      )
    },
  ) { innerPadding ->
    ModelList(
      task = task,
      modelManagerViewModel = viewModel,
      contentPadding = innerPadding,
      enableAnimation = enableAnimation,
      onImportModelClicked = { filePickerLauncher.launch(arrayOf("*/*")) },
      onModelClicked = onModelClicked,
      onBenchmarkClicked = onBenchmarkClicked,
      modifier = Modifier.fillMaxSize(),
    )
  }

  if (showImportDialog) {
    selectedLocalModelUri?.let { uri ->
      ModelImportDialog(
        uri = uri,
        onDismiss = { showImportDialog = false },
        onDone = { info ->
          selectedImportedModelInfo = info
          showImportDialog = false
          showImportingDialog = true
        },
      )
    }
  }

  if (showImportingDialog) {
    val uri = selectedLocalModelUri
    val info = selectedImportedModelInfo
    if (uri != null && info != null) {
      ModelImportingDialog(
        uri = uri,
        info = info,
        onDismiss = { showImportingDialog = false },
        onDone = {
          viewModel.addImportedLlmModel(it)
          showImportingDialog = false
        },
      )
    }
  }
}
