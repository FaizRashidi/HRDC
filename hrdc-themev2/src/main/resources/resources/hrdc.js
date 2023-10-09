const header = `
                <div class="main-header"">
                    <img src="https://hrdcorp.gov.my/wp-content/uploads/2021/02/MoHR_HRDCorp_logo_website.svg">
                </div>`


const appendTable = (mutationList) => {
    let table = $('.table-wrapper table')
    let filter = $('.filter_form[id^=filters]')
    let footer = $('.table-wrapper').parent()
    let tableContainer = $('.table-mari')
    
    let filterHeight, footerHeight, deductHeight
    
    if (mutationList.length > 0) {
        if(tableContainer.length == 0){
            $(filter).after('<div class="table-mari"></div>')
            $(table).appendTo('.table-mari')
            
            setTimeout(function(){
                
                if($(filter).length > 0){
                    console.log('filter exist')
                    filterHeight = $(filter).height()
                }

                if($(footer).length > 0){
                    console.log('footer exist')
                    footerHeight = $(footer).height()
                }

                deductHeight = filterHeight + footerHeight + 333
                
                $('.table-mari').css(`height`, `calc(100vh - ${deductHeight}px)`)
                
            }, 300)
            
            
        }
      
    }
}

const targetNode = document.getElementById("category-container");
const config = { attributes: true, childList: true, subtree: true };
const observer = new MutationObserver(appendTable);


$(function(){
    $('body:not(".popupBody")').prepend(header)
    
    if ($("#form-canvas").length > 0) {
        $("#form-canvas").find(".form-cell, .subform-cell").each(function(){
            var label = $(this).find("> label.label").text();
            $(this).find("> textarea, > input, > select").attr("placeholder", label);
        });
    }
    
    //observer.observe(targetNode, config);
    
});