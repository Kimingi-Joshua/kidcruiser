<?php
namespace Flutterwave;

//uncomment if you need this
//define("BASEPATH", 1);//Allow direct access to rave.php and raveEventHandler.php

require_once('rave.php');
require_once('EventHandlerInterface.php');

use Flutterwave\Rave;
use Flutterwave\EventHandlerInterface;

class transferEventHandler implements EventHandlerInterface{
    /**
     * This is called only when a transaction is successful 
     * @param array
     * */
    function onSuccessful($transactionData){
        // Get the transaction from your DB using the transaction reference (txref)
        // Check if you have previously given value for the transaction. If you have, redirect to your successpage else, continue
        // Comfirm that the transaction is successful
        // Confirm that the chargecode is 00 or 0
        // Confirm that the currency on your db transaction is equal to the returned currency
        // Confirm that the db transaction amount is equal to the returned amount
        // Update the db transaction record (includeing parameters that didn't exist before the transaction is completed. for audit purpose)
        // Give value for the transaction
        // Update the transaction to note that you have given value for the transaction
        // You can also redirect to your success page from here
    }
    
    /**
     * This is called only when a transaction failed
     * */
    function onFailure($transactionData){
        // Get the transaction from your DB using the transaction reference (txref)
        // Update the db transaction record (includeing parameters that didn't exist before the transaction is completed. for audit purpose)
        // You can also redirect to your failure page from here
       
    }
    
    /**
     * This is called when a transaction is requeryed from the payment gateway
     * */
    function onRequery($transactionReference){
        // Do something, anything!
    }
    
    /**
     * This is called a transaction requery returns with an error
     * */
    function onRequeryError($requeryResponse){
        // Do something, anything!
    }
    
    /**
     * This is called when a transaction is canceled by the user
     * */
    function onCancel($transactionReference){
        // Do something, anything!
        // Note: Somethings a payment can be successful, before a user clicks the cancel button so proceed with caution
       
    }
    
    /**
     * This is called when a transaction doesn't return with a success or a failure response. This can be a timedout transaction on the Rave server or an abandoned transaction by the customer.
     * */
    function onTimeout($transactionReference, $data){
        // Get the transaction from your DB using the transaction reference (txref)
        // Queue it for requery. Preferably using a queue system. The requery should be about 15 minutes after.
        // Ask the customer to contact your support and you should escalate this issue to the flutterwave support team. Send this as an email and as a notification on the page. just incase the page timesout or disconnects
      
    }
}

class Transfer {
    protected $transfer;
    function __construct(){
        $this->transfer = new Rave($_ENV['SECRET_KEY']);
    }
    //initiating a single transfer
    function singleTransfer($array){
        //set the payment handler 
        $this->transfer->eventHandler(new transferEventHandler)
        //set the endpoint for the api call
        ->setEndPoint("v3/transfers");
        //returns the value from the results
        return $this->transfer->transferSingle($array);
    }

     //initiating a bulk transfer
    function bulkTransfer($array){
        //set the payment handler 
        $this->transfer->eventHandler(new transferEventHandler)
        //set the endpoint for the api call
        ->setEndPoint("v3/bulk-transfers");
        //returns the value from the results
        return $this->transfer->transferBulk($array);
    }

    function listTransfers($array = array('url'=>'blank')){
            $this->transfer->eventHandler(new transferEventHandler)
        //set the endpoint for the api call
            ->setEndPoint("v3/transfers");

        return $this->transfer->listTransfers($array);
        

        //set the payment handler 
        
    }

    function bulkTransferStatus($array){

         //set the payment handler 
         $this->transfer->eventHandler(new transferEventHandler)
         //set the endpoint for the api call
         ->setEndPoint("v3/bulk-transfers");

         return $this->transfer->bulkTransferStatus($array);
    }
    function getTransferFee($array){

        if(in_array('amount', $array) && gettype($array['amount']) !== "string"){
            $array['amount'] = (string) $array['amount'];
        }

         //set the payment handler 
         $this->transfer->eventHandler(new transferEventHandler)
         //set the endpoint for the api call
         ->setEndPoint("v3/transfers/fee");

         return $this->transfer->applicableFees($array);
    }



    function getBanksForTransfer($data = array("country" => 'NG')){
        
           //set the payment handler 
           $this->transfer->eventHandler(new transferEventHandler)
           //set the endpoint for the api call

           ->setEndPoint("v2/banks/".$data['country']."/");
        
        
        return $this->transfer->getBanksForTransfer();
    }


    function verifyTransaction(){
        //verify the charge
        return $this->transfer->verifyTransaction($this->transfer->txref);//Uncomment this line if you need it
    }




}

?>