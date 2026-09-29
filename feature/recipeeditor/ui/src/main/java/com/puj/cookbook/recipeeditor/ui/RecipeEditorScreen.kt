package com.puj.cookbook.recipeeditor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.designsystem.R as DesignSystemR
import com.puj.cookbook.designsystem.RecipePicture
import com.puj.cookbook.designsystem.formatClock
import com.puj.cookbook.recipes.domain.CheckItem
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.RecipeDefaults
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock

/** Быстрые пресеты длительности таймера в минутах. */
private val TIMER_PRESETS_MINUTES = listOf(1, 5, 10, 30)

/** Куда импортировать выбранное изображение: обложка блюда или конкретный блок. */
private sealed interface PickTarget {
    /** Общая картинка блюда. */
    data object Cover : PickTarget

    /** Изображение внутри блока с индексом [index]. */
    data class Block(val index: Int) : PickTarget
}

/**
 * Экран создания и редактирования рецепта: обложка блюда, заголовок, описание, блоки
 * и их сохранение.
 *
 * @param onSaved вызывается с идентификатором сохранённого рецепта.
 * @param onCancel закрывает экран без сохранения.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    onSaved: (Long) -> Unit,
    onCancel: () -> Unit,
) {
    val vm: RecipeEditorViewModel = hiltViewModel()

    val recipe by vm.recipe.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val defaultChecklistTitle = stringResource(R.string.editor_default_checklist_title)
    val defaultTimerLabel = stringResource(R.string.editor_default_timer_label)

    var pickTarget by remember { mutableStateOf<PickTarget?>(null) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            when (val target = pickTarget) {
                is PickTarget.Cover -> vm.importCoverImage(uri, queryDisplayName(uri))
                is PickTarget.Block -> vm.importPicture(target.index, uri, queryDisplayName(uri))
                null -> vm.importPicture(null, uri, queryDisplayName(uri))
            }
        }
        pickTarget = null
    }
    val launchPicker: (PickTarget?) -> Unit = { target ->
        pickTarget = target
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (vm.isEditing) R.string.editor_title_edit else R.string.editor_title_new))
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
                },
                actions = {
                    TextButton(onClick = { vm.save(onSaved) }, enabled = !saving) {
                        Text(stringResource(R.string.action_save))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CoverEditor(
                    coverPath = recipe.coverImagePath,
                    onPick = { launchPicker(PickTarget.Cover) },
                    onRemove = vm::removeCoverImage,
                )
            }
            item {
                OutlinedTextField(
                    value = recipe.title,
                    onValueChange = vm::setTitle,
                    label = { Text(stringResource(R.string.editor_field_title)) },
                    singleLine = true,
                    isError = error == R.string.editor_error_title_required,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = recipe.description,
                    onValueChange = vm::setDescription,
                    label = { Text(stringResource(R.string.editor_field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
            error?.let { errorRes ->
                item {
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            itemsIndexed(recipe.blocks) { index, block ->
                EditableBlockView(
                    index = index,
                    block = block,
                    vm = vm,
                    onPickImage = { launchPicker(PickTarget.Block(index)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                AddBlockRow(
                    onText = vm::addText,
                    onPicture = { launchPicker(null) },
                    onChecklist = { vm.addChecklist(defaultChecklistTitle) },
                    onTimer = { vm.addTimer(defaultTimerLabel, RecipeDefaults.DEFAULT_TIMER_SECONDS) },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun queryDisplayName(uri: Uri): String? = uri.lastPathSegment

/** Секция общей картинки блюда (обложки) с выбором, заменой и удалением. */
@Composable
private fun CoverEditor(
    coverPath: String?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.editor_cover_label), style = MaterialTheme.typography.labelLarge)

        if (coverPath != null) {
            RecipePicture(path = coverPath, contentDescription = null)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_photo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPick, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(
                        if (coverPath == null) R.string.editor_cover_add else R.string.editor_cover_change
                    )
                )
            }
            if (coverPath != null) {
                TextButton(onClick = onRemove) {
                    Text(
                        text = stringResource(R.string.editor_cover_remove),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/** Ряд кнопок для добавления новых блоков в рецепт. */
@Composable
private fun AddBlockRow(
    onText: () -> Unit,
    onPicture: () -> Unit,
    onChecklist: () -> Unit,
    onTimer: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.editor_add_section), style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            BlockChip(R.string.editor_add_text, DesignSystemR.drawable.ic_text, onClick = onText)
            BlockChip(R.string.editor_add_picture, DesignSystemR.drawable.ic_photo, onClick = onPicture)
            BlockChip(R.string.editor_add_checklist, DesignSystemR.drawable.ic_checklist, onClick = onChecklist)
            BlockChip(R.string.editor_add_timer, DesignSystemR.drawable.ic_timer, onClick = onTimer)
        }
    }
}

/** Кнопка-чип добавления блока с иконкой и подписью. */
@Composable
private fun BlockChip(labelRes: Int, iconRes: Int, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(stringResource(labelRes)) },
        leadingIcon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}

/** Карточка редактирования одного блока с кнопками перемещения и удаления. */
@Composable
private fun EditableBlockView(
    index: Int,
    block: RecipeBlock,
    vm: RecipeEditorViewModel,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (block) {
                is TextBlock -> TextBlockEditor(index, block, vm)
                is PictureBlock -> PictureBlockEditor(index, block, vm, onPickImage)
                is ChecklistBlock -> ChecklistBlockEditor(index, block, vm)
                is TimerBlock -> TimerBlockEditor(index, block, vm)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.moveUp(index) }, enabled = index > 0) {
                    Text(stringResource(R.string.editor_move_up))
                }
                TextButton(onClick = { vm.moveDown(index) }, enabled = index < vm.recipe.value.blocks.lastIndex) {
                    Text(stringResource(R.string.editor_move_down))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { vm.removeBlock(index) }) {
                    Text(stringResource(R.string.editor_delete_block), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Редактор текстового блока. */
@Composable
private fun TextBlockEditor(index: Int, block: TextBlock, vm: RecipeEditorViewModel) {
    OutlinedTextField(
        value = block.text,
        onValueChange = { vm.updateTextBlock(index, it) },
        label = { Text(stringResource(R.string.editor_instruction)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
}

/** Редактор блока-изображения: предпросмотр, выбор/замена картинки и подпись. */
@Composable
private fun PictureBlockEditor(
    index: Int,
    block: PictureBlock,
    vm: RecipeEditorViewModel,
    onPickImage: () -> Unit,
) {
    RecipePicture(path = block.imagePath, contentDescription = block.caption)
    OutlinedButton(onClick = onPickImage, modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(
                if (block.imagePath.isBlank()) R.string.editor_pick_image else R.string.editor_replace_image
            )
        )
    }
    OutlinedTextField(
        value = block.caption,
        onValueChange = { vm.updatePictureCaption(index, it) },
        label = { Text(stringResource(R.string.editor_caption)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Редактор чеклиста: заголовок, пункты и кнопки добавления/удаления пунктов. */
@Composable
private fun ChecklistBlockEditor(index: Int, block: ChecklistBlock, vm: RecipeEditorViewModel) {
    OutlinedTextField(
        value = block.title,
        onValueChange = { vm.updateChecklistTitle(index, it) },
        label = { Text(stringResource(R.string.editor_checklist_title)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    block.items.forEachIndexed { itemIndex, item: CheckItem ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = item.text,
                onValueChange = { vm.updateCheckItem(index, itemIndex, it) },
                label = { Text(stringResource(R.string.editor_item_number, itemIndex + 1)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { vm.removeCheckItem(index, itemIndex) }) {
                Text(stringResource(R.string.editor_remove_item))
            }
        }
    }
    OutlinedButton(onClick = { vm.addCheckItem(index) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.editor_add_item))
    }
}

/** Редактор таймера: подпись, пресеты длительности и точный ввод минут и секунд. */
@Composable
private fun TimerBlockEditor(index: Int, block: TimerBlock, vm: RecipeEditorViewModel) {
    OutlinedTextField(
        value = block.label,
        onValueChange = { vm.updateTimerLabel(index, it) },
        label = { Text(stringResource(R.string.editor_timer_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.editor_duration, formatClock(block.seconds)),
        style = MaterialTheme.typography.bodyMedium,
    )

    var minutes by remember(index) { mutableStateOf((block.seconds / 60).toString()) }
    var seconds by remember(index) { mutableStateOf((block.seconds % 60).toString()) }

    fun commit(min: String, sec: String) {
        val m = min.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
        val s = sec.toLongOrNull()?.coerceIn(0L, 59L) ?: 0L
        vm.updateTimerSeconds(index, m * 60 + s)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        TIMER_PRESETS_MINUTES.forEach { preset ->
            AssistChip(
                onClick = {
                    minutes = preset.toString()
                    seconds = "0"
                    commit(minutes, seconds)
                },
                label = { Text("$preset ${stringResource(R.string.editor_minutes)}") },
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = minutes,
            onValueChange = { minutes = it; commit(it, seconds) },
            label = { Text(stringResource(R.string.editor_minutes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = seconds,
            onValueChange = { seconds = it; commit(minutes, it) },
            label = { Text(stringResource(R.string.editor_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}
