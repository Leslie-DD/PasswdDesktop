package passwds.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import passwds.entity.Group
import passwds.entity.Passwd
import passwds.model.PasswdsViewModel
import passwds.model.TranslateScreenUiAction

/**
 * 密码界面主要内容的显示区域
 */
@Composable
fun PasswdsScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = Modifier.fillMaxSize()) {
        PasswdGroupList(viewModel = viewModel, modifier = Modifier.width(250.dp))
        Divider(
            modifier = Modifier
                .padding(0.dp, 10.dp)
                .fillMaxHeight()
                .width(1.dp)
        )
        PasswdItemsContent(viewModel = viewModel)
    }
}

/**
 * 密码分组 List
 */
@Composable
fun PasswdGroupList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            items(viewModel.translateUiState.groups) { group ->
                GroupCard(
                    group = group,
                    isSelected = group.id == viewModel.uiState.selectGroup?.id
                ) {
                    viewModel.onAction(TranslateScreenUiAction.ShowGroupPasswds(groupId = it))
                }
            }
        }
    }
}

/**
 * 包含密码 List 和密码详情两部分
 */
@Composable
fun PasswdItemsContent(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            PasswdItemsList(viewModel = viewModel, modifier = Modifier.width(250.dp))
            Divider(
                modifier = Modifier
                    .padding(0.dp, 10.dp)
                    .fillMaxHeight()  //fill the max height
                    .width(1.dp)
            )
            PasswdDetailScreen(viewModel = viewModel)
        }
    }
}

/**
 * 密码 List（显示指定分组下的所有密码，每条 item 只显示 title 和 username）
 */
@Composable
fun PasswdItemsList(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 10.dp)
    ) {
        items(viewModel.translateUiState.groupPasswds) { passwd ->
            PasswdCard(
                passwd = passwd,
                isSelected = passwd.id == viewModel.uiState.selectPasswd?.id
            ) {
                viewModel.onAction(TranslateScreenUiAction.ShowPasswd(passwdId = it))
            }
        }
    }
}

/**
 * 密码详情
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswdDetailScreen(
    viewModel: PasswdsViewModel,
    modifier: Modifier = Modifier
) {
    val passwd = viewModel.uiState.selectPasswd
    Box(modifier.fillMaxSize().padding(16.dp)) {
        passwd?.let {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("title", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null)
                    },
                    value = passwd.title ?: "",
                    maxLines = 1,
                    onValueChange = { },
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("username", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                    },
                    value = passwd.usernameString ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("password", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    value = passwd.passwordString ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("link", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null)
                    },
                    value = passwd.link ?: "",
                    maxLines = 1,
                    onValueChange = { }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxSize().align(Alignment.Start),
                    label = { Text("comment", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    value = passwd.comment ?: "",
                    onValueChange = { }
                )
            }
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(end = 10.dp, top = 5.dp, bottom = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isSelected
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CutCornerShape(bottomEndPercent = 40, topEndPercent = 60)
                    )
                )
            }
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { NoRippleInteractionSource() },
            onClick = { onClick(group.id) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Spacer(modifier = Modifier.width(15.dp).fillMaxHeight())
            Text(text = group.groupName ?: "", fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.weight(0.6f))
        }
    }
}

@Composable
fun PasswdCard(
    passwd: Passwd,
    isSelected: Boolean,
    onClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(70.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = isSelected
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CutCornerShape(bottomEndPercent = 40, topEndPercent = 60)
                    )
                )
            }
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            interactionSource = remember { NoRippleInteractionSource() },
            onClick = { onClick(passwd.id) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(0.1f))
                Text(text = passwd.title ?: "", fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(0.1f))
                Text(
                    text = passwd.usernameString ?: "",
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(0.1f))
            }
        }
    }
}