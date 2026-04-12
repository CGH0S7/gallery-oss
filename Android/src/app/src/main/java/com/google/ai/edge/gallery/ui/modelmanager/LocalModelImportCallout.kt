package com.google.ai.edge.gallery.ui.modelmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LocalModelImportCallout(onImportClick: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.padding(18.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Outlined.NoteAdd,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Import local model",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      Text(
        text =
          "This build only supports offline local model files. Online recommendations and downloads are disabled.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ImportInfoPill(text = "Supported model type: LiteRT-LM LLM")
        ImportInfoPill(text = "Supported import formats: .litertlm, .task")
      }
      Button(onClick = onImportClick, modifier = Modifier.fillMaxWidth()) {
        Text("Import local model")
      }
    }
  }
}

@Composable
private fun ImportInfoPill(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier =
      Modifier.background(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(999.dp),
        )
        .padding(horizontal = 12.dp, vertical = 8.dp),
  )
}
