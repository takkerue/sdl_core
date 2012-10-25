#ifndef DELETECOMMAND_RESPONSE_INCLUDE
#define DELETECOMMAND_RESPONSE_INCLUDE

#include <string>

#include "Result.h"
#include "JSONHandler/ALRPCResponse.h"


/*
  interface	Ford Sync RAPI
  version	1.2
  date		2011-05-17
  generated at	Thu Oct 25 04:31:05 2012
  source stamp	Wed Oct 24 14:57:16 2012
  author	robok0der
*/


class DeleteCommand_response : public ALRPCResponse
{
public:

  DeleteCommand_response(const DeleteCommand_response& c);
  DeleteCommand_response(void);
  
  virtual ~DeleteCommand_response(void);

  DeleteCommand_response& operator =(const DeleteCommand_response&);

  bool checkIntegrity(void);

  bool get_success(void) const;
  const Result& get_resultCode(void) const;
  const std::string* get_info(void) const;

  bool set_success(bool success_);
  bool set_resultCode(const Result& resultCode_);
  void reset_info(void);
  bool set_info(const std::string& info_);

private:

  friend class DeleteCommand_responseMarshaller;

  bool success;
  Result resultCode;
  std::string* info;	//!< (1000)
};

#endif
