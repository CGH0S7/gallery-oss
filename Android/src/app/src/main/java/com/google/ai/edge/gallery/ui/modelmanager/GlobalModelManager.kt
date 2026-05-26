package com.google.ai.edge.gallery.ui.modelmanager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.ai.edge.gallery.R
import com.google.ai.edge.gallery.data.Model
import com.google.ai.edge.gallery.data.Task
import com.google.ai.edge.gallery.proto.ImportedModel
import com.google.ai.edge.gallery.ui.common.modelitem.ModelItem
import kotlinx.coroutines.launch

private const val TAG = "AGGlobalMM"
private val GLOBAL_MODEL_MANAGER_CONTENT_MAX_WIDTH = 960.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalModelManager(
  viewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  onModelSelected: (Task, Model) -> Unit,
  onBenchmarkClicked: (Model) -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()
  val importedModels =
    remember(uiState.modelImportingUpdateTrigger, uiState.tasks) {
      uiState.tasks
        .flatMap { it.models }
        .filter { it.imported }
        .distinctBy { it.name }
        .sortedBy { it.displayName.ifEmpty { it.name } }
    }
  var showUnsupportedFileTypeDialog by remember { mutableStateOf(false) }
  var showUnsupportedWebModelDialog by remember { mutableStateOf(false) }
  var selectedLocalModelUri by remember { mutableStateOf<Uri?>(null) }
  var selectedImportedModelInfo by remember { mutableStateOf<ImportedModel?>(null) }
  var showImportDialog by remember { mutableStateOf(false) }
  var showImportingDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val filePickerLauncher: ActivityResultLauncher<Intent> =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
      if (result.resultCode == android.app.Activity.RESULT_OK) {
        result.data?.data?.let { uri ->
          val fileName = getFileName(context = context, uri = uri)
          Log.d(TAG, "Selected file: $fileName")
          if (fileName != null && !fileName.endsWith(".task") && !fileName.endsWith(".litertlm")) {
            showUnsupportedFileTypeDialog = true
          } else if (fileName != null && fileName.lowercase().contains("-web")) {
            showUnsupportedWebModelDialog = true
          } else {
            selectedLocalModelUri = uri
            showImportDialog = true
          }
        }
      }
    }

  val openPicker = {
    val intent =
      Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
      }
    filePickerLauncher.launch(intent)
  }

  BackHandler { navigateUp() }

  Scaffold(
    modifier = modifier,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${stringResource(R.string.drawer_models_label)} (${importedModels.size})",
              color = MaterialTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium,
            )
          }
        },
        actions = {
          IconButton(onClick = { navigateUp() }) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = stringResource(R.string.cd_close_icon),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        },
      )
    },
    floatingActionButton = {
      val cdImportModelFab = stringResource(R.string.cd_import_model_button)
      SmallFloatingActionButton(
        onClick = openPicker,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.semantics { contentDescription = cdImportModelFab },
      ) {
        Icon(Icons.Filled.Add, contentDescription = null)
      }
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box {
      Box(
        contentAlignment = Alignment.TopCenter,
        modifier =
          Modifier.background(MaterialTheme.colorScheme.surfaceContainer).fillMaxWidth(),
      ) {
        LazyColumn(
          modifier =
            Modifier.widthIn(max = GLOBAL_MODEL_MANAGER_CONTENT_MAX_WIDTH)
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(top = innerPadding.calculateTopPadding()),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding =
            PaddingValues(top = 16.dp, bottom = innerPadding.calculateBottomPadding() + 80.dp),
        ) {
          item(key = "import_callout") { LocalModelImportCallout(onImportClick = openPicker) }

          if (importedModels.isNotEmpty()) {
            item(key = "imported_models_label") {
              Text(
                stringResource(R.string.model_list_imported_models_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                modifier =
                  Modifier.padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 8.dp),
              )
            }
          }

          items(importedModels) { model ->
            ModelItem(
              model = model,
              task = null,
              modelManagerViewModel = viewModel,
              onModelClicked = { clickedModel ->
                val task =
                  uiState.tasks.firstOrNull { cur -> cur.models.any { it.name == clickedModel.name } }
                if (task != null) {
                  onModelSelected(task, clickedModel)
                }
              },
              onBenchmarkClicked = onBenchmarkClicked,
              expanded = true,
              showBenchmarkButton = true,
            )
          }
        }
      }

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(innerPadding.calculateBottomPadding())
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceContainer)
              )
            )
            .align(Alignment.BottomCenter)
      )
    }
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
          scope.launch { snackbarHostState.showSnackbar("Model imported successfully") }
        },
      )
    }
  }

  if (showUnsupportedFileTypeDialog) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = stringResource(R.string.cd_error),
          tint = MaterialTheme.colorScheme.error,
        )
      },
      onDismissRequest = { showUnsupportedFileTypeDialog = false },
      title = { Text("Unsupported file type") },
      text = { Text("Only \".task\" or \".litertlm\" file type is supported.") },
      confirmButton = {
        Button(onClick = { showUnsupportedFileTypeDialog = false }) {
          Text(stringResource(R.string.ok))
        }
      },
    )
  }

  if (showUnsupportedWebModelDialog) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = stringResource(R.string.cd_error),
          tint = MaterialTheme.colorScheme.error,
        )
      },
      onDismissRequest = { showUnsupportedWebModelDialog = false },
      title = { Text("Unsupported model type") },
      text = { Text("Looks like the model is a web-only model and is not supported by the app.") },
      confirmButton = {
        Button(onClick = { showUnsupportedWebModelDialog = false }) {
          Text(stringResource(R.string.ok))
        }
      },
    )
  }
}

private fun getFileName(context: Context, uri: Uri): String? {
  if (uri.scheme == "content") {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1) {
          return cursor.getString(nameIndex)
        }
      }
    }
  } else if (uri.scheme == "file") {
    return uri.lastPathSegment
  }
  return null
}
