# dktspringapp

## Files
This folder contains the initial configurations for the agent's beliefs and desires as well as the agent's knowledge. The files are .csv files. You can modify them
as you wish. 
- Belief schema: belief-id,belief-name,phase,belief-value,is-modifiable,reason
- Desire schema: desire-id,desire-name,desire-status
- Knowledge schema: subject,attribute,value-1, value-2,value-3,value-4

The belief schema's last two fields are optional. When a belief is set to be not modifiable, it means the trainer should
not be able to modify them in the Training Portal on the frontend. All code is backward compatible with the old belief 
format that didn't include the last two fields. The following table shows examples of belief records in the belief schema
and their corresponding boolean value for isModifiable:

| Belief as seen in .csv file                                                    | evaluates to | note                                                            |
|--------------------------------------------------------------------------------|--------------|-----------------------------------------------------------------|
| B1,Ik denk dat KT mij begrijpt,alle fasen,0                                    | true         | Old format, still works                                         |
| B2,Ik denk dat KT mij begrijpt,alle fasen,0,                                   | true         | Empty becomes true                                              |
| B3,Ik denk dat KT mij begrijpt,alle fasen,0,true                               | true         | New format, used correctly                                      |
| B4,Ik denk dat KT mij begrijpt,alle fasen,0,,                                  | true         | Empty becomes true                                              |
| B5,Ik denk dat KT mij begrijpt,alle fasen,0,true,                              | true         | Empty reason is never used                                      |
| B6,Ik denk dat KT mij begrijpt,alle fasen,0,true,Depends on nothing            | true         | Nonempty reason is never used                                   |
| B7,Ik denk dat KT mij begrijpt,alle fasen,0,false                              | false        | No reason will display 'reason unknown'                         |
| B8,Ik denk dat KT mij begrijpt,alle fasen,0,false,                             | false        | Empty reason will display empty reason                          |
| B9,Ik denk dat KT mij begrijpt,alle fasen,0,false,,                            | false        | Empty reason will display empty reason                          |
| B10,Ik denk dat KT mij begrijpt,alle fasen,0,falsee                            | true         | All except 'f' or 'false', case insensitive, becomes true       |
| B11,Ik denk dat KT mij begrijpt,alle fasen,0,false,Depends on B4, B5, B6, B7   | false        | Reason becomes "Depends on B4" due to commas without quotations |
| B12,Ik denk dat KT mij begrijpt,alle fasen,0,false,Depends on B4 and B5        | false        | Reason works fine without quotations when no commas are used    |
| B13,Ik denk dat KT mij begrijpt,alle fasen,0,false,"Depends on B4, B5, B6, B7" | false        | New format, used correctly                                      |
| B14,Ik denk dat KT mij begrijpt,alle fasen,0,f,"Depends on B4, B5, B6, B7"     | false        | 'f' or 'F' becomes false                                        |
| B15,Ik denk dat KT mij begrijpt,alle fasen,0,fAlsE,"Depends on B4, B5, B6, B7" | false        | Case insensitive                                                |

With knowledge, the agent chooses one of the 4 values at random as it's response.


## Agent Service
This is the main BDI application. It depends on the Belief Service, Desire Service and Knowledge Service to create and manage BDI agents. I currently use a switch statement for the intent-belief mapping but I recommend you change this (in future work) if the number of beliefs and intents increase. 

## Report Service
This is the class for generating a transcript of the conversation with Lilobot together with the BDI updates as feedback. Currently, the configuration
is set to store the document on your local machine. If you want to store the transcripts in Azure Blob Storage, follow these steps:
- Under resources > application.properties, change the Azure configuration to your Azure Blob storage account name and key. You can find your storage account's connection strings in the Azure portal. Navigate to SECURITY + NETWORKING > Access keys in your storage account's menu blade to see them.
- In ReportService, set ```localMode``` to ```false```.
- Replace ```connectionString``` to the connection string to your Azure storage account.
  You can find your storage account's connection strings in the Azure portal. Navigate to SETTINGS > Access keys in your storage account's menu blade to see connection strings for both primary and secondary access keys.
  More info: https://docs.microsoft.com/en-us/azure/storage/common/storage-configure-connection-string
- In the Rasa application, navigate to actions.py. Set ````LOCALMODE```` to ``False``.

## Configuration
application.properties contains the configuration for the postgres database and Azure storage account.
- Postgres: localhost:5432/dktbase
- Azure Storage account name and key
