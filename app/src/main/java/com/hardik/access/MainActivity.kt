package com.hardik.access

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.hardik.access.ads.ConsentManager
import com.hardik.access.ui.theme.AdMintTheme
import com.hardik.access.ui.theme.AquaGlow
import com.hardik.access.ui.theme.ElectricBlue
import com.hardik.access.ui.theme.Midnight
import com.hardik.access.ui.theme.Mint
import com.hardik.access.ui.theme.NightBlue
import com.hardik.access.ui.theme.SoftPurple
import com.hardik.access.ui.theme.TextSecondary
import java.text.DecimalFormat
import kotlin.math.pow

class MainActivity : ComponentActivity() {

    private val interstitialFrequency = 4
    private var interstitialAd: InterstitialAd? = null
    private var actionCounter: Int = 0
    private var mobileAdsInitialized = false

    private lateinit var consentManager: ConsentManager

    private var adServingEnabled by mutableStateOf(false)
    private var privacyOptionsRequired by mutableStateOf(false)
    private var consentStatusText by mutableStateOf("Requesting ad consent...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consentManager = ConsentManager(this)

        setContent {
            AdMintTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FinanceToolkitApp(
                        adServingEnabled = adServingEnabled,
                        privacyOptionsRequired = privacyOptionsRequired,
                        consentStatusText = consentStatusText,
                        onAdEligibleAction = {
                            if (adServingEnabled) {
                                actionCounter += 1
                                if (actionCounter % interstitialFrequency == 0) {
                                    maybeShowInterstitial()
                                }
                            }
                        },
                        onOpenPrivacyOptions = {
                            consentManager.showPrivacyOptionsForm {
                                privacyOptionsRequired = consentManager.isPrivacyOptionsRequired()
                            }
                        },
                        onOpenPolicyLink = { openPrivacyPolicyUrl() }
                    )
                }
            }
        }

        requestConsentAndMaybeInitializeAds()
    }

    private fun requestConsentAndMaybeInitializeAds() {
        consentManager.requestConsent { canRequestAds, optionsRequired ->
            privacyOptionsRequired = optionsRequired
            if (canRequestAds) {
                consentStatusText = "Ads enabled"
                initializeAdsIfNeeded()
            } else {
                adServingEnabled = false
                consentStatusText = "Ads waiting for consent"
            }
        }
    }

    private fun initializeAdsIfNeeded() {
        if (mobileAdsInitialized) {
            adServingEnabled = true
            return
        }
        mobileAdsInitialized = true
        MobileAds.initialize(this)
        adServingEnabled = true
        loadInterstitialAd()
    }

    private fun maybeShowInterstitial() {
        val currentAd = interstitialAd ?: return
        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                loadInterstitialAd()
            }

            override fun onAdShowedFullScreenContent() {
                interstitialAd = null
            }
        }
        currentAd.show(this)
    }

    private fun loadInterstitialAd() {
        if (!adServingEnabled) return
        InterstitialAd.load(
            this,
            getString(R.string.admob_interstitial_unit_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun openPrivacyPolicyUrl() {
        val url = BuildConfig.PRIVACY_POLICY_URL
        if (!url.startsWith("http")) {
            Toast.makeText(this, "Privacy policy URL is not configured", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No browser found to open privacy policy", Toast.LENGTH_SHORT).show()
        }
    }
}

private enum class Tool(val label: String, val icon: ImageVector) {
    Tip("Tip Split", Icons.Default.TipsAndUpdates),
    Discount("Discount", Icons.Default.LocalOffer),
    Emi("EMI Loan", Icons.Default.Payments),
    Gst("GST", Icons.Default.Percent)
}

@Composable
private fun FinanceToolkitApp(
    adServingEnabled: Boolean,
    privacyOptionsRequired: Boolean,
    consentStatusText: String,
    onAdEligibleAction: () -> Unit,
    onOpenPrivacyOptions: () -> Unit,
    onOpenPolicyLink: () -> Unit
) {
    var selectedTool by rememberSaveable { mutableStateOf(Tool.Tip) }
    var showPolicyDialog by rememberSaveable { mutableStateOf(false) }
    val policyText = rememberPrivacyPolicyText()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(NightBlue, Midnight, Color(0xFF111B39)),
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (adServingEnabled) {
                    BannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "Ads unavailable until consent is complete",
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeaderCard(
                    consentStatusText = consentStatusText,
                    privacyOptionsRequired = privacyOptionsRequired,
                    onOpenPrivacyOptions = onOpenPrivacyOptions,
                    onShowPolicy = { showPolicyDialog = true },
                    onOpenPolicyLink = onOpenPolicyLink
                )
                ToolPicker(selected = selectedTool, onSelected = { selectedTool = it })
                CalculatorBody(selectedTool = selectedTool, onAdEligibleAction = onAdEligibleAction)
            }
        }
    }

    if (showPolicyDialog) {
        PrivacyPolicyDialog(
            policyText = policyText,
            onDismiss = { showPolicyDialog = false },
            onOpenPolicyLink = onOpenPolicyLink
        )
    }
}

@Composable
private fun HeaderCard(
    consentStatusText: String,
    privacyOptionsRequired: Boolean,
    onOpenPrivacyOptions: () -> Unit,
    onShowPolicy: () -> Unit,
    onOpenPolicyLink: () -> Unit
) {
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "AdMint Finance Toolkit",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Useful daily calculators with premium UX and policy-ready ad monetization.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FeaturePill("Dark Mode")
                FeaturePill("Glass UI")
                FeaturePill("Fast Results")
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Consent: $consentStatusText", color = TextSecondary)
                    Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = "Consent", tint = AquaGlow)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShowPolicy) {
                    Text("Read Policy")
                }
                OutlinedButton(onClick = onOpenPolicyLink) {
                    Text("Open URL")
                }
                if (privacyOptionsRequired) {
                    OutlinedButton(onClick = onOpenPrivacyOptions) {
                        Text("Ad Privacy Options")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyDialog(policyText: String, onDismiss: () -> Unit, onOpenPolicyLink: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Privacy Policy") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = policyText, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = onOpenPolicyLink) { Text("Open web version") }
        }
    )
}

@Composable
private fun rememberPrivacyPolicyText(): String {
    val context = LocalContext.current
    return remember {
        runCatching {
            context.resources
                .openRawResource(R.raw.privacy_policy)
                .bufferedReader()
                .use { it.readText() }
        }.getOrElse { context.getString(R.string.privacy_policy_summary) }
    }
}

@Composable
private fun FeaturePill(text: String) {
    Box(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = ElectricBlue, spotColor = SoftPurple)
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun ToolPicker(selected: Tool, onSelected: (Tool) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Tool.entries.forEach { tool ->
            val isSelected = selected == tool
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(tool) },
                label = { Text(tool.label) },
                leadingIcon = {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.label,
                        modifier = Modifier.width(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.07f),
                    selectedContainerColor = ElectricBlue.copy(alpha = 0.34f),
                    selectedLabelColor = Color.White,
                    labelColor = TextSecondary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) AquaGlow.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.14f)
                )
            )
        }
    }
}

@Composable
private fun CalculatorBody(selectedTool: Tool, onAdEligibleAction: () -> Unit) {
    when (selectedTool) {
        Tool.Tip -> TipCalculator(onAdEligibleAction)
        Tool.Discount -> DiscountCalculator(onAdEligibleAction)
        Tool.Emi -> EmiCalculator(onAdEligibleAction)
        Tool.Gst -> GstCalculator(onAdEligibleAction)
    }
}

@Composable
private fun TipCalculator(onAdEligibleAction: () -> Unit) {
    var billAmount by rememberSaveable { mutableStateOf("") }
    var tipPercent by rememberSaveable { mutableStateOf("10") }
    var splitBy by rememberSaveable { mutableStateOf("1") }
    var showResult by rememberSaveable { mutableStateOf(false) }

    val bill = billAmount.toDoubleOrNull().orZero()
    val tip = tipPercent.toDoubleOrNull().orZero()
    val split = splitBy.toIntOrNull().orOne()

    val totalTip = bill * (tip / 100.0)
    val totalPay = bill + totalTip
    val perPerson = if (split <= 0) totalPay else totalPay / split

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Tip + Split Calculator", Icons.Default.TipsAndUpdates)
            DecimalField("Bill amount", billAmount) { billAmount = sanitizeDecimalInput(it) }
            DecimalField("Tip %", tipPercent) { tipPercent = sanitizeDecimalInput(it) }
            IntegerField("Split by people", splitBy) { splitBy = sanitizeIntegerInput(it) }
            Button(
                onClick = {
                    showResult = true
                    onAdEligibleAction()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Calculate")
            }
            if (showResult) {
                ResultLine("Tip amount", formatMoney(totalTip))
                ResultLine("Total bill", formatMoney(totalPay))
                ResultLine("Per person", formatMoney(perPerson), accent = Mint)
            }
        }
    }
}

@Composable
private fun DiscountCalculator(onAdEligibleAction: () -> Unit) {
    var originalPrice by rememberSaveable { mutableStateOf("") }
    var discountPercent by rememberSaveable { mutableStateOf("15") }
    var taxPercent by rememberSaveable { mutableStateOf("0") }
    var showResult by rememberSaveable { mutableStateOf(false) }

    val price = originalPrice.toDoubleOrNull().orZero()
    val discount = discountPercent.toDoubleOrNull().orZero()
    val tax = taxPercent.toDoubleOrNull().orZero()

    val discountValue = price * (discount / 100.0)
    val discountedPrice = price - discountValue
    val taxValue = discountedPrice * (tax / 100.0)
    val finalPayable = discountedPrice + taxValue

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Discount Calculator", Icons.Default.LocalOffer)
            DecimalField("Original price", originalPrice) { originalPrice = sanitizeDecimalInput(it) }
            DecimalField("Discount %", discountPercent) { discountPercent = sanitizeDecimalInput(it) }
            DecimalField("Tax % (optional)", taxPercent) { taxPercent = sanitizeDecimalInput(it) }
            Button(
                onClick = {
                    showResult = true
                    onAdEligibleAction()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SoftPurple)
            ) {
                Text("Get savings")
            }
            if (showResult) {
                ResultLine("You save", formatMoney(discountValue), accent = Mint)
                ResultLine("Price after discount", formatMoney(discountedPrice))
                ResultLine("Final payable", formatMoney(finalPayable), accent = Color.White)
            }
        }
    }
}

@Composable
private fun EmiCalculator(onAdEligibleAction: () -> Unit) {
    var principalInput by rememberSaveable { mutableStateOf("") }
    var annualRateInput by rememberSaveable { mutableStateOf("8.5") }
    var tenureYearsInput by rememberSaveable { mutableStateOf("5") }
    var showResult by rememberSaveable { mutableStateOf(false) }

    val principal = principalInput.toDoubleOrNull().orZero()
    val annualRate = annualRateInput.toDoubleOrNull().orZero()
    val years = tenureYearsInput.toIntOrNull().orOne()

    val months = (years * 12).coerceAtLeast(1)
    val monthlyRate = annualRate / 1200.0

    val emi = if (monthlyRate == 0.0) {
        principal / months
    } else {
        val growth = (1.0 + monthlyRate).pow(months)
        principal * monthlyRate * growth / (growth - 1)
    }.safeFinite()
    val totalPayment = (emi * months).safeFinite()
    val totalInterest = (totalPayment - principal).safeFinite()

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("EMI Calculator", Icons.Default.Payments)
            DecimalField("Loan amount", principalInput) { principalInput = sanitizeDecimalInput(it) }
            DecimalField("Interest rate % (annual)", annualRateInput) { annualRateInput = sanitizeDecimalInput(it) }
            IntegerField("Tenure in years", tenureYearsInput) { tenureYearsInput = sanitizeIntegerInput(it) }
            Button(
                onClick = {
                    showResult = true
                    onAdEligibleAction()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AquaGlow.copy(alpha = 0.36f))
            ) {
                Text("Compute EMI")
            }
            if (showResult) {
                ResultLine("Monthly EMI", formatMoney(emi), accent = Mint)
                ResultLine("Total interest", formatMoney(totalInterest))
                ResultLine("Total payment", formatMoney(totalPayment))
            }
        }
    }
}

@Composable
private fun GstCalculator(onAdEligibleAction: () -> Unit) {
    var amountInput by rememberSaveable { mutableStateOf("") }
    var gstInput by rememberSaveable { mutableStateOf("18") }
    var inclusiveMode by rememberSaveable { mutableStateOf(false) }
    var showResult by rememberSaveable { mutableStateOf(false) }

    val amount = amountInput.toDoubleOrNull().orZero()
    val gstRate = gstInput.toDoubleOrNull().orZero()

    val gstValue: Double
    val taxableValue: Double
    val totalAmount: Double

    if (inclusiveMode) {
        taxableValue = amount / (1 + (gstRate / 100.0))
        gstValue = amount - taxableValue
        totalAmount = amount
    } else {
        taxableValue = amount
        gstValue = amount * (gstRate / 100.0)
        totalAmount = amount + gstValue
    }

    GlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("GST Calculator", Icons.Default.Percent)
            DecimalField("Amount", amountInput) { amountInput = sanitizeDecimalInput(it) }
            DecimalField("GST %", gstInput) { gstInput = sanitizeDecimalInput(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeChip(label = "Add GST", selected = !inclusiveMode) {
                    inclusiveMode = false
                }
                ModeChip(label = "GST Included", selected = inclusiveMode) {
                    inclusiveMode = true
                }
            }
            Button(
                onClick = {
                    showResult = true
                    onAdEligibleAction()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.75f))
            ) {
                Text("Calculate GST")
            }
            if (showResult) {
                ResultLine("Taxable amount", formatMoney(taxableValue.safeFinite()))
                ResultLine("GST amount", formatMoney(gstValue.safeFinite()), accent = Mint)
                ResultLine("Total amount", formatMoney(totalAmount.safeFinite()))
            }
        }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) ElectricBlue.copy(alpha = 0.33f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (selected) AquaGlow.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.18f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = if (selected) Color.White else TextSecondary)
    }
}

@Composable
private fun SectionTitle(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = text, tint = AquaGlow)
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White
        )
    }
}

@Composable
private fun DecimalField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun IntegerField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun ResultLine(title: String, value: String, accent: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = accent,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(20.dp), ambientColor = ElectricBlue, spotColor = SoftPurple),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.16f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val adView = remember {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = context.getString(R.string.admob_banner_unit_id)
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        adView.loadAd(AdRequest.Builder().build())

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sponsored",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            AndroidView(
                factory = { adView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
        }
    }
}

private fun sanitizeDecimalInput(input: String): String {
    val cleaned = input.filter { it.isDigit() || it == '.' }
    val firstDot = cleaned.indexOf('.')
    return if (firstDot == -1) cleaned else {
        val head = cleaned.substring(0, firstDot + 1)
        val tail = cleaned.substring(firstDot + 1).replace(".", "")
        head + tail
    }
}

private fun sanitizeIntegerInput(input: String): String = input.filter { it.isDigit() }

private fun Double?.orZero(): Double = this ?: 0.0

private fun Int?.orOne(): Int = (this ?: 1).coerceAtLeast(1)

private fun Double.safeFinite(): Double = if (this.isFinite()) this else 0.0

private fun formatMoney(value: Double): String = "$" + DecimalFormat("#,##0.00").format(value)
