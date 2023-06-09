<?php
namespace Flutterwave;

//uncomment if you need this
//define("BASEPATH", 1);//Allow direct access to rave.php and raveEventHandler.php

require_once('rave.php');
require_once('EventHandlerInterface.php');

use Flutterwave\Rave;
use Flutterwave\EventHandlerInterface;

class accountEventHandler implements EventHandlerInterface{
    /**
     * This is called only when a transaction is successful
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
        if($transactionData["data"]["chargecode"] === '00' || $transactionData["data"]["chargecode"] === '0'){
          echo "Transaction Completed";
      }else{
          $this->onFailure($transactionData);
      }
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


class Account {
    protected $payment;

    function __construct(){
        $this->payment = new Rave($_ENV['SECRET_KEY']);
        $this->type = array('debit_uk_account','debit_ng_account');
        $this->valType = "account";
    }
    
    function accountCharge($array){
            //set the payment handler 

    //add tx_ref to the paylaod
    if(!isset($array['tx_ref']) || empty($array['tx_ref'])){
        $array['tx_ref'] = $this->payment->txref;
    }else{
        $this->payment->txref = $array['tx_ref'];   
    }


    if(!in_array($array['type'], $this->type)){
            echo '<div class="alert alert-danger" role="alert"> <b>Error:</b> 
            The Type specified in the payload  is not <b> "'.$this->type[0].' or '.$this->type[1].'"</b>
          </div>';
    }


            $this->payment->eventHandler(new accountEventHandler);
            //set the endpoint for the api call
            if ($this->type === $this->type[0]){
                $this->payment->setEndPoint("v3/charges?type=debit_uk_account");
            }else{
                $this->payment->setEndPoint("v3/charges?type=debit_ng_account");
            }

            
            return $this->payment->chargePayment($array);
           
        }
    function validateTransaction($otp, $ref){
            //validate the charge
        $this->payment->eventHandler(new accountEventHandler);

        return $this->payment->validateTransaction($otp, $ref, $this->payment->type);//Uncomment this line if you need it
        
       }

       function return_txref(){
        return $this->payment->txref;
    }
       
    function verifyTransaction($id){
        //verify the charge
        return $this->payment->verifyTransaction($id);//Uncomment this line if you need it

    }
    }

    
?>