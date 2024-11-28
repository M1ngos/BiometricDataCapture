import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun SignatureScreenWrapper(
    viewModel: SignatureViewModel = viewModel(),
    navController: NavHostController
) {
    SignatureScreen(
        onSignatureSaved = { file ->
            viewModel.onSignatureSaved(file)
        }
    )
}