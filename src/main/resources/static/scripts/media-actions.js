function onMediaActionFormSubmit(form) {
    try {
        document.querySelectorAll('.media-action-button')
                .forEach(button => {
                    button.setAttribute('disabled', 'disabled')
                })

        form.querySelectorAll('.media-action-spinner')
                .forEach(spinner => {
                    spinner.classList.remove("d-none")
                })

        return true;
    } catch (e) {
        alert("An error happened")
        return false;
    }
}
