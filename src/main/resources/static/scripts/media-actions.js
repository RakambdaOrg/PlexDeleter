function onMediaActionFormSubmit() {
    document.querySelectorAll('.media-action-button')
            .forEach(btn2 => {
                btn2.setAttribute('disabled', 'disabled')
            })
    return true;
}
