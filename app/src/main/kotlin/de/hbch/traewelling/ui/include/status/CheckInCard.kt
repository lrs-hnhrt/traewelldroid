package de.hbch.traewelling.ui.include.status

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hbch.traewelling.R
import de.hbch.traewelling.api.models.status.Status
import de.hbch.traewelling.api.models.status.StatusBusiness
import de.hbch.traewelling.api.models.status.StatusVisibility
import de.hbch.traewelling.api.models.trip.HafasTrainTripStation
import de.hbch.traewelling.api.models.trip.ProductType
import de.hbch.traewelling.shared.LoggedInUserViewModel
import de.hbch.traewelling.shared.SettingsViewModel
import de.hbch.traewelling.theme.HeartRed
import de.hbch.traewelling.theme.LocalColorScheme
import de.hbch.traewelling.theme.LocalFont
import de.hbch.traewelling.theme.StarYellow
import de.hbch.traewelling.ui.composables.CustomClickableText
import de.hbch.traewelling.ui.composables.Dialog
import de.hbch.traewelling.ui.composables.LineIcon
import de.hbch.traewelling.ui.composables.ProfilePicture
import de.hbch.traewelling.ui.composables.SharePicDialog
import de.hbch.traewelling.ui.report.Report
import de.hbch.traewelling.ui.tag.StatusTags
import de.hbch.traewelling.ui.user.getDurationString
import de.hbch.traewelling.util.getLocalDateTimeString
import de.hbch.traewelling.util.getLocalTimeString
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun CheckInCard(
    modifier: Modifier = Modifier,
    checkInCardViewModel: CheckInCardViewModel,
    status: Status?,
    joinConnection: (Status) -> Unit,
    loggedInUserViewModel: LoggedInUserViewModel? = null,
    displayLongDate: Boolean = false,
    stationSelected: (Int, ZonedDateTime?) -> Unit = { _, _ -> },
    userSelected: (String) -> Unit = { },
    statusSelected: (Int) -> Unit = { },
    handleEditClicked: (Status) -> Unit = { },
    onDeleted: (Status) -> Unit = { }
) {
    val primaryColor = LocalColorScheme.current.primary

    if (status != null) {
        val statusClickedAction: () -> Unit = {
            statusSelected(status.id)
        }

        var progress by remember { mutableFloatStateOf(0f) }
        val progressAnimation by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "AnimateCheckInCardProgress",
        )
        LaunchedEffect(true) {
            while (true) {
                progress = calculateProgress(
                    from = status.journey.departureManual ?: status.journey.origin.departureReal
                    ?: status.journey.origin.departurePlanned,
                    to = status.journey.arrivalManual ?: status.journey.destination.arrivalReal
                    ?: status.journey.destination.arrivalPlanned
                )
                delay(5000)
            }
        }

        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            onClick = statusClickedAction
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val (
                        perlschnurTop,
                        perlschnurConnection,
                        perlschnurBottom,
                        stationRowTop,
                        stationRowBottom,
                        content
                    ) = createRefs()

                    // Perlschnur
                    Icon(
                        modifier = Modifier
                            .constrainAs(perlschnurTop) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            }
                            .size(20.dp),
                        painter = painterResource(id = R.drawable.ic_perlschnur_main),
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Image(
                        modifier = Modifier.constrainAs(perlschnurConnection) {
                            start.linkTo(perlschnurTop.start)
                            end.linkTo(perlschnurTop.end)
                            top.linkTo(perlschnurTop.bottom)
                            bottom.linkTo(perlschnurBottom.top)
                            height = Dimension.fillToConstraints
                            width = Dimension.value(2.dp)
                        },
                        painter = painterResource(id = R.drawable.ic_perlschnur_connection),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Icon(
                        modifier = Modifier
                            .constrainAs(perlschnurBottom) {
                                start.linkTo(parent.start)
                                bottom.linkTo(parent.bottom)
                            }
                            .size(20.dp),
                        painter = painterResource(id = R.drawable.ic_perlschnur_main),
                        contentDescription = null,
                        tint = primaryColor
                    )

                    // Station row top
                    StationRow(
                        modifier = Modifier.constrainAs(stationRowTop) {
                            start.linkTo(perlschnurTop.end, 8.dp)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            width = Dimension.fillToConstraints
                        },
                        station = status.journey.destination,
                        timePlanned = status.journey.destination.arrivalPlanned,
                        timeReal = status.journey.arrivalManual ?: status.journey.destination.arrivalReal,

                        stationSelected = stationSelected
                    )

                    // Station row bottom
                    StationRow(
                        modifier = Modifier
                            .constrainAs(stationRowBottom) {
                                start.linkTo(perlschnurBottom.end, 8.dp)
                                end.linkTo(parent.end)
                                bottom.linkTo(parent.bottom)
                                width = Dimension.fillToConstraints
                            },
                        station = status.journey.origin,
                        timePlanned = status.journey.origin.departurePlanned,
                        timeReal = status.journey.departureManual ?: status.journey.origin.departureReal,

                        verticalAlignment = Alignment.Bottom,
                        stationSelected = stationSelected
                    )

                    // Main content
                    CheckInCardContent(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .constrainAs(content) {
                                top.linkTo(stationRowTop.bottom)
                                bottom.linkTo(stationRowBottom.top)
                                start.linkTo(stationRowTop.start)
                                end.linkTo(stationRowTop.end)
                                width = Dimension.fillToConstraints
                            },
                        productType = status.journey.safeProductType,
                        line = status.journey.line,
                        kilometers = status.journey.distance,
                        duration = status.journey.duration,
                        statusBusiness = status.business,
                        message = status.getStatusBody(),
                        journeyNumber = status.journey.journeyNumber,
                        operatorCode = status.journey.operator?.id,
                        lineId = status.journey.lineId,
                        userSelected = userSelected,
                        textClicked = statusClickedAction
                    )
                }
                LinearProgressIndicator(
                    progress = {
                        if (progressAnimation.isNaN()) 1f else progressAnimation
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
                CheckInCardFooter(
                    modifier = Modifier.fillMaxWidth(),
                    status = status,
                    joinConnection = joinConnection,
                    isOwnStatus =
                    (loggedInUserViewModel?.loggedInUser?.value?.id ?: -1) == status.user.id,
                    displayLongDate = displayLongDate,
                    checkInCardViewModel = checkInCardViewModel,
                    userSelected = userSelected,
                    handleEditClicked = {
                        handleEditClicked(status)
                    },
                    handleDeleteClicked = {
                        checkInCardViewModel.deleteStatus(status.id, {
                            onDeleted(status)
                        }, { })
                    },
                    defaultVisibility = loggedInUserViewModel?.defaultStatusVisibility ?: StatusVisibility.PUBLIC
                )
            }
        }
    }
}

fun calculateProgress(
    from: ZonedDateTime,
    to: ZonedDateTime
): Float {
    val currentDate = ZonedDateTime.now()
    // Default cases
    if (currentDate > to) {
        return 1f
    } else if (currentDate < from) {
        return 0f
    }

    val fromZoned = from.toInstant().toEpochMilli()
    val toZoned = to.toInstant().toEpochMilli()
    val currentZoned = currentDate.toInstant().toEpochMilli()

    val fullTimeSpanMillis = toZoned - fromZoned
    val elapsedTimeSpanMillis = currentZoned - fromZoned

    return elapsedTimeSpanMillis.toFloat() / fullTimeSpanMillis.toFloat()
}

@Composable
fun StationRow(
    modifier: Modifier = Modifier,
    station: HafasTrainTripStation,
    timePlanned: ZonedDateTime,
    timeReal: ZonedDateTime?,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    stationSelected: (Int, ZonedDateTime?) -> Unit = { _, _ -> }
) {
    val primaryColor = LocalColorScheme.current.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = verticalAlignment
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier
                    .clickable { stationSelected(station.id, null) },
                text = station.name,
                style = LocalFont.current.titleLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = primaryColor
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            val hasDelay = !Duration.between(timePlanned, timeReal ?: timePlanned).isZero
            val displayedDate =
                if (hasDelay && timeReal != null)
                    timeReal
                else
                    timePlanned
            Text(
                modifier = Modifier.clickable { stationSelected(station.id, displayedDate) },
                text = getLocalTimeString(
                    date = displayedDate
                ),
                color = primaryColor,
                style = LocalFont.current.titleLarge
            )
            if (hasDelay) {
                Text(
                    text = getLocalTimeString(
                        date = timePlanned
                    ),
                    textDecoration = TextDecoration.LineThrough,
                    style = LocalFont.current.labelLarge
                )
            }
        }
    }
}

@Composable
fun CheckInCardContent(
    modifier: Modifier = Modifier,
    productType: ProductType,
    line: String,
    journeyNumber: Int?,
    kilometers: Int,
    duration: Int,
    statusBusiness: StatusBusiness,
    message: Pair<AnnotatedString?, Map<String, InlineTextContent>>,
    operatorCode: String? = null,
    lineId: String? = null,
    userSelected: (String) -> Unit = { },
    textClicked: () -> Unit = { }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusDetailsRow(
            productType = productType,
            line = line,
            journeyNumber = journeyNumber,
            kilometers = kilometers,
            duration = duration,
            statusBusiness = statusBusiness,
            operatorCode = operatorCode,
            lineId = lineId
        )
        if (!message.first.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_quote),
                    contentDescription = null
                )
                CustomClickableText(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = message.first!!,
                    onClick = {
                        val annotations = message.first!!.getStringAnnotations(it - 1, it + 1)
                        if (annotations.isNotEmpty()) {
                            userSelected(annotations.first().item)
                        } else {
                            textClicked()
                        }
                    },
                    style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                    inlineContent = message.second
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusDetailsRow(
    productType: ProductType,
    line: String,
    journeyNumber: Int?,
    kilometers: Int,
    duration: Int,
    statusBusiness: StatusBusiness,
    modifier: Modifier = Modifier,
    operatorCode: String? = null,
    lineId: String? = null
) {
    FlowRow(
        modifier = modifier
    ) {
        val alignmentModifier = Modifier.align(Alignment.CenterVertically)
        Image(
            modifier = alignmentModifier,
            painter = painterResource(id = productType.getIcon()),
            contentDescription = null
        )
        LineIcon(
            lineName = line,
            modifier = alignmentModifier.padding(start = 4.dp),
            operatorCode = operatorCode,
            lineId = lineId,
            journeyNumber = journeyNumber
        )
        Text(
            modifier = alignmentModifier.padding(start = 12.dp),
            text = getFormattedDistance(kilometers),
            style = LocalFont.current.bodySmall
        )
        Text(
            modifier = alignmentModifier.padding(start = 8.dp),
            text = getDurationString(duration = duration),
            style = LocalFont.current.bodySmall
        )
        Icon(
            modifier = alignmentModifier.padding(start = 8.dp),
            painter = painterResource(id = statusBusiness.icon),
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CheckInCardFooter(
    modifier: Modifier = Modifier,
    status: Status,
    checkInCardViewModel: CheckInCardViewModel,
    joinConnection: (Status) -> Unit,
    isOwnStatus: Boolean = false,
    displayLongDate: Boolean = false,
    defaultVisibility: StatusVisibility = StatusVisibility.PUBLIC,
    userSelected: (String) -> Unit = { },
    handleEditClicked: () -> Unit = { },
    handleDeleteClicked: () -> Unit = { }
) {
    var likedState by remember { mutableStateOf(status.liked ?: false) }
    var likeCountState by remember { mutableIntStateOf(status.likes ?: 0) }
    var reportFormVisible by remember { mutableStateOf(false) }
    var shareVisible by remember { mutableStateOf(false) }

    if (reportFormVisible) {
        Dialog(
            onDismissRequest = {
                reportFormVisible = false
            }
        ) {
            Report(
                statusId = status.id,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    if (shareVisible) {
        Dialog(
            onDismissRequest = {
                shareVisible = false
            }
        ) {
            SharePicDialog(status = status)
        }
    }

    val settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ViewModelStoreOwner
    )
    val displayTagsState by settingsViewModel.displayTagsInCard.observeAsState(true)

    if (displayTagsState) {
        StatusTags(
            tags = status.tags,
            statusId = status.id,
            modifier = Modifier.fillMaxWidth(),
            isOwnStatus = isOwnStatus,
            defaultVisibility = defaultVisibility
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status.liked != null && status.likes != null && status.likeable == true) {
                Row(
                    modifier = Modifier
                        .clickable {
                            if (likedState) {
                                checkInCardViewModel.deleteFavorite(status.id) {
                                    likedState = false
                                    likeCountState--
                                }
                            } else {
                                checkInCardViewModel.createFavorite(status.id) {
                                    likedState = true
                                    likeCountState++
                                }
                            }
                        }
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedContent(
                        targetState = likedState,
                        label = "FavoriteAnimation"
                    ) {
                        val color = if (status.isTraewelldroidCheckIn) HeartRed else StarYellow
                        val icon = when (status.isTraewelldroidCheckIn) {
                            true -> if (it) R.drawable.ic_heart_filled else R.drawable.ic_heart
                            false -> if (it) R.drawable.ic_faved else R.drawable.ic_not_faved
                        }
                        Icon(
                            painterResource(id = icon),
                            contentDescription = null,
                            tint = color
                        )
                    }
                    Text(
                        text = likeCountState.toString()
                    )
                }
            } else {
                Box { }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                val alignmentModifier = Modifier.align(Alignment.CenterVertically)
                val dateString =
                    if (displayLongDate)
                        getLocalDateTimeString(date = status.createdAt)
                    else
                        getLocalTimeString(date = status.createdAt)
                ProfilePicture(
                    name = status.user.username,
                    url = status.user.avatarUrl,
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp)
                        .padding(end = 2.dp)
                )
                Text(
                    modifier = alignmentModifier
                        .clickable { userSelected(status.user.username) }
                        .padding(2.dp),
                    text = stringResource(
                        id = R.string.check_in_user_time,
                        status.user.username,
                        dateString
                    ),
                    textAlign = TextAlign.End,
                    style = LocalFont.current.labelLarge
                )
                Icon(
                    modifier = alignmentModifier.padding(horizontal = 8.dp),
                    painter = painterResource(id = status.visibility.icon),
                    contentDescription = null
                )
            }
            var menuExpanded by remember { mutableStateOf(false) }
            val context = LocalContext.current
            Box {
                Icon(
                    modifier = Modifier
                        .clickable {
                            menuExpanded = true
                        }
                        .padding(2.dp),
                    painter = painterResource(id = R.drawable.ic_more),
                    contentDescription = null,
                    tint = LocalColorScheme.current.primary
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    if (isOwnStatus) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.title_share)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_share),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                shareVisible = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.title_edit)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = null
                                )
                            },
                            onClick = handleEditClicked
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.delete)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                handleDeleteClicked()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.title_also_check_in)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_also_check_in),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                joinConnection(status)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.title_report)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_report),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                reportFormVisible = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Event
    if (!status.event?.name.isNullOrEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null
            )
            Text(
                text = status.event!!.name,
                style = LocalFont.current.labelMedium
            )
        }
    }
}

@Composable
fun getFormattedDistance(distance: Int): String {
    val roundedDistance =
        if (distance < 1000)
            Measure(distance, MeasureUnit.METER)
        else
            Measure(distance / 1000, MeasureUnit.KILOMETER)

    return MeasureFormat
        .getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT)
        .formatMeasures(roundedDistance)
}
