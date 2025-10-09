console.log("Content script loaded");

function createAIbutton() {
    const button = document.createElement('button');
    button.className = 'T-I J-J5-Ji ao0 v7 T-I-atl L3';
    button.style.marginRight = '8px';
    button.innerHTML = 'AI Reply';
    button.setAttribute('role', 'button');
    button.setAttribute('data-tooltip', 'AI Reply');
    return button;
}

function findComposeToolbar(){
    const selectors = [".btc", ".aDh", "[role='toolbar'], .gU.Up"];
   for(const selector of selectors) {
    const toolbar = document.querySelector(selector);
    if(toolbar) {
        return toolbar;
    }else{
        return null;
    }
}
}

function getemailcontent(){
    const selectors = ['.h7', '.a3s,aiL', 'gmail_quote', '[role="presentation"]'];
   for(const selector of selectors) {
    const content = document.querySelector(selector);
    if(content) {
        return content.innerText.trim();
    }else{
        return '';
    }
}
}

function injectButton() {
    const existingButton = document.getElementById('.ai-reply-button');
    if(existingButton) {
        existingButton.remove();
    } const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("toolbar not found");
        return;
    }
    console.log("toolbar found, creating ai button");
    const button = createAIbutton();
    button.classList.add('ai-reply-button');
    button.addEventListener('click', async() => {
        try{
            button.innerHTML = 'Generating...';
            button.disabled = true;

            const mailcontent=getemailcontent();
            await fetch('http://localhost:8080/api/email/generate', {
                method: 'POST',
                headers: {  
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ emailContent: mailcontent , tone:"professional" })
            });
            if(!Response.ok){
                throw new Error('Api request failed');
            }
            const generatedReply = await response.text();
            const composebox = document.querySelector('[role="textbox"],[g_editable="true"]');
            if(composebox){
                composebox.focus();
                document.execCommand('insertText', false, generatedReply);
            }else{
                console.error("Compose box not found");
            }
        } catch(error){
            console.error("Error generating AI reply:", error);
        }finally{
            button.innerHTML = 'AI Reply';
            button.disabled = false;
        }
        // Here you can add the logic to generate AI reply
    });
    toolbar.insertBefore(button, toolbar.firstChild);
}

const observer = new MutationObserver((mutations)=> {
    for (const mutation of mutations) {
        const addedNodes = Array.from(mutation.addedNodes);
        const  hasComposeElement = addedNodes.some(node => node.nodeType === Node.ELEMENT_NODE && (node.matches('.aDh, .btc,[role="dialog"]') || node.querySelector('.aDh, .btc,[role="dialog"]')));
        if (hasComposeElement) {
            console.log("Compose email element added");
            setTimeout(injectButton, 500);
        }
    }
});

observer.observe(document.body, { childList: true, subtree: true });
