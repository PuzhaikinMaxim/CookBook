package com.puj.cookbook.recipeeditor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.core.CookBookButton
import com.puj.cookbook.core.CookBookButtonStyle
import com.puj.cookbook.core.CookBookCard
import com.puj.cookbook.core.CookBookChip
import com.puj.cookbook.core.CookBookScaffold
import com.puj.cookbook.core.CookBookText
import com.puj.cookbook.core.CookBookTextField
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.CookBookTopBar
import com.puj.cookbook.core.R as CoreR
import com.puj.cookbook.core.RecipePicture
import com.puj.cookbook.core.formatClock
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

    CookBookScaffold(
        topBar = {
            CookBookTopBar(
                title = stringResource(if (vm.isEditing) R.string.editor_title_edit else R.string.editor_title_new),
                navigation = {
                    CookBookButton(
                        text = stringResource(R.string.action_cancel),
                        onClick = onCancel,
                        buttonStyle = CookBookButtonStyle.Text,
                    )
                },
                actions = {
                    CookBookButton(
                        text = stringResource(R.string.action_save),
                        onClick = { vm.save(onSaved) },
                        enabled = !saving,
                        buttonStyle = CookBookButtonStyle.Text,
                    )
                },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = 32.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CoverEditor(
                    coverPath = recipe.coverImagePath,
                    onPick = { launchPicker(PickTarget.Cover) },
                    onRemove = vm::removeCoverImage,
                )
            }
            item {
                CookBookTextField(
                    value = recipe.title,
                    onValueChange = vm::setTitle,
                    label = stringResource(R.string.editor_field_title),
                    singleLine = true,
                    isError = error == R.string.editor_error_title_required,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                CookBookTextField(
                    value = recipe.description,
                    onValueChange = vm::setDescription,
                    label = stringResource(R.string.editor_field_description),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            error?.let { errorRes ->
                item {
                    CookBookText(
                        text = stringResource(errorRes),
                        style = CookBookTheme.typography.bodySmall,
                        color = CookBookTheme.colors.error,
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CookBookText(
            text = stringResource(R.string.editor_cover_label),
            style = CookBookTheme.typography.label,
            color = CookBookTheme.colors.textSecondary,
        )

        if (coverPath != null) {
            RecipePicture(path = coverPath, contentDescription = null)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(CookBookTheme.shapes.large)
                    .background(CookBookTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(CoreR.drawable.ic_photo),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(CookBookTheme.colors.textSecondary),
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CookBookButton(
                text = stringResource(
                    if (coverPath == null) R.string.editor_cover_add else R.string.editor_cover_change
                ),
                onClick = onPick,
                buttonStyle = CookBookButtonStyle.Outlined,
                modifier = Modifier.weight(1f),
            )
            if (coverPath != null) {
                CookBookButton(
                    text = stringResource(R.string.editor_cover_remove),
                    onClick = onRemove,
                    buttonStyle = CookBookButtonStyle.Text,
                )
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
        CookBookText(
            text = stringResource(R.string.editor_add_section),
            style = CookBookTheme.typography.label,
            color = CookBookTheme.colors.textSecondary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            BlockChip(R.string.editor_add_text, CoreR.drawable.ic_text, onClick = onText)
            BlockChip(R.string.editor_add_picture, CoreR.drawable.ic_photo, onClick = onPicture)
            BlockChip(R.string.editor_add_checklist, CoreR.drawable.ic_checklist, onClick = onChecklist)
            BlockChip(R.string.editor_add_timer, CoreR.drawable.ic_timer, onClick = onTimer)
        }
    }
}

/** Кнопка-чип добавления блока с иконкой и подписью. */
@Composable
private fun BlockChip(labelRes: Int, iconRes: Int, onClick: () -> Unit) {
    CookBookChip(
        label = stringResource(labelRes),
        onClick = onClick,
        leadingPainter = painterResource(iconRes),
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
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    CookBookCard(modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (block) {
                is TextBlock -> TextBlockEditor(index, block, vm)
                is PictureBlock -> PictureBlockEditor(index, block, vm, onPickImage)
                is ChecklistBlock -> ChecklistBlockEditor(index, block, vm)
                is TimerBlock -> TimerBlockEditor(index, block, vm)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CookBookButton(
                    text = stringResource(R.string.editor_move_up),
                    onClick = { vm.moveUp(index) },
                    enabled = index > 0,
                    buttonStyle = CookBookButtonStyle.Text,
                )
                CookBookButton(
                    text = stringResource(R.string.editor_move_down),
                    onClick = { vm.moveDown(index) },
                    enabled = index < recipe.blocks.lastIndex,
                    buttonStyle = CookBookButtonStyle.Text,
                )
                Spacer(Modifier.weight(1f))
                CookBookButton(
                    text = stringResource(R.string.editor_delete_block),
                    onClick = { vm.removeBlock(index) },
                    buttonStyle = CookBookButtonStyle.Text,
                )
            }
        }
    }
}

/** Редактор текстового блока. */
@Composable
private fun TextBlockEditor(index: Int, block: TextBlock, vm: RecipeEditorViewModel) {
    CookBookTextField(
        value = block.text,
        onValueChange = { vm.updateTextBlock(index, it) },
        label = stringResource(R.string.editor_instruction),
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
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
    CookBookButton(
        text = stringResource(
            if (block.imagePath.isBlank()) R.string.editor_pick_image else R.string.editor_replace_image
        ),
        onClick = onPickImage,
        buttonStyle = CookBookButtonStyle.Outlined,
        modifier = Modifier.fillMaxWidth(),
    )
    CookBookTextField(
        value = block.caption,
        onValueChange = { vm.updatePictureCaption(index, it) },
        label = stringResource(R.string.editor_caption),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Редактор чеклиста: заголовок, пункты и кнопки добавления/удаления пунктов. */
@Composable
private fun ChecklistBlockEditor(index: Int, block: ChecklistBlock, vm: RecipeEditorViewModel) {
    CookBookTextField(
        value = block.title,
        onValueChange = { vm.updateChecklistTitle(index, it) },
        label = stringResource(R.string.editor_checklist_title),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    block.items.forEachIndexed { itemIndex, item: CheckItem ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            CookBookTextField(
                value = item.text,
                onValueChange = { vm.updateCheckItem(index, itemIndex, it) },
                label = stringResource(R.string.editor_item_number, itemIndex + 1),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            CookBookButton(
                text = stringResource(R.string.editor_remove_item),
                onClick = { vm.removeCheckItem(index, itemIndex) },
                buttonStyle = CookBookButtonStyle.Text,
            )
        }
    }
    CookBookButton(
        text = stringResource(R.string.editor_add_item),
        onClick = { vm.addCheckItem(index) },
        buttonStyle = CookBookButtonStyle.Outlined,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Редактор таймера: подпись, пресеты длительности и точный ввод минут и секунд. */
@Composable
private fun TimerBlockEditor(index: Int, block: TimerBlock, vm: RecipeEditorViewModel) {
    CookBookTextField(
        value = block.label,
        onValueChange = { vm.updateTimerLabel(index, it) },
        label = stringResource(R.string.editor_timer_label),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    CookBookText(
        text = stringResource(R.string.editor_duration, formatClock(block.seconds)),
        style = CookBookTheme.typography.bodySmall,
        color = CookBookTheme.colors.textSecondary,
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
            CookBookChip(
                label = "$preset ${stringResource(R.string.editor_minutes)}",
                onClick = {
                    minutes = preset.toString()
                    seconds = "0"
                    commit(minutes, seconds)
                },
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CookBookTextField(
            value = minutes,
            onValueChange = { minutes = it; commit(it, seconds) },
            label = stringResource(R.string.editor_minutes),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        CookBookTextField(
            value = seconds,
            onValueChange = { seconds = it; commit(minutes, it) },
            label = stringResource(R.string.editor_seconds),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
    }
}
