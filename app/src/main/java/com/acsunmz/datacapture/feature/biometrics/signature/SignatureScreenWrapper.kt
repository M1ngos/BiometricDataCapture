import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.acsunmz.datacapture.core.presentation.navigation.Destinations

@Composable
fun SignatureScreenWrapper(
    viewModel: SignatureViewModel = viewModel(),
    navController: NavHostController
) {
    SignatureScreen(
        onSignatureSaved = { file ->
            viewModel.onSignatureSaved(file)
            navController.navigate(Destinations.SendCaptureDataScreen)
        }
    )
}